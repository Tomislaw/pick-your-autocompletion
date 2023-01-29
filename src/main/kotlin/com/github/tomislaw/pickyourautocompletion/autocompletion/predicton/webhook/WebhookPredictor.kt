package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.webhook

import com.github.tomislaw.pickyourautocompletion.autocompletion.context.Prompt
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.Predictor
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.webhook.parser.BodyParser
import com.github.tomislaw.pickyourautocompletion.autocompletion.template.VariableTemplateParser
import com.github.tomislaw.pickyourautocompletion.errors.*
import com.github.tomislaw.pickyourautocompletion.settings.data.PromptBuilderData
import com.github.tomislaw.pickyourautocompletion.settings.data.WebRequestBuilderData
import kotlinx.coroutines.*
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import okhttp3.internal.http2.ConnectionShutdownException
import org.apache.commons.text.StringEscapeUtils
import org.apache.commons.text.translate.CharSequenceTranslator
import org.jetbrains.concurrency.await
import org.jetbrains.concurrency.isPending
import org.jetbrains.concurrency.runAsync
import java.io.IOException
import java.net.SocketTimeoutException
import java.net.UnknownHostException
import java.nio.charset.Charset
import java.time.Duration

class WebhookPredictor(request: WebRequestBuilderData) : Predictor {
    private val client: OkHttpClient

    private val parser: BodyParser?
    private val variableParser = VariableTemplateParser()

    private val bodyTemplate: String
    private val charset: Charset
    private val mediaType: MediaType?

    private val url: String
    private val headers: Collection<Pair<String, String>>
    private val method: String

    private val translator: CharSequenceTranslator

    private val minimumDelayBetweenRequestsInMillis: Int
    private var lastTimeWhenInvoked: Long = 0

    init {
        client = OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(request.timeoutInMillis.toLong()))
            .build()
        parser = request.bodyParser
        bodyTemplate = request.bodyTemplate
        charset = request.charset
        mediaType = request.contentType.runCatching { toMediaType() }.getOrNull()
        url = request.url
        headers = request.headers
        method = request.method
        minimumDelayBetweenRequestsInMillis = request.minimumDelayInMillis
        translator = StringEscapeUtils.ESCAPE_JSON
    }

    override suspend fun predict(prompt: Prompt, tokens: Int, stop: List<String>): Result<String> {

        if (parser == null)
            return Result.success("")

        // update variable parser with special properties
        variableParser.setVariable("prompt", translator.translate(prompt.text))
        variableParser.setVariable("prompt.${PromptBuilderData.LANGUAGE}", translator.translate(prompt.language))
        variableParser.setVariable("prompt.${PromptBuilderData.FILE}", translator.translate(prompt.file))
        variableParser.setVariable("prompt.${PromptBuilderData.DIRECTORY}", translator.translate(prompt.directory))
        variableParser.setVariable("prompt.${PromptBuilderData.TEXT_AFTER}", translator.translate(prompt.textAfter))
        variableParser.setVariable("prompt.${PromptBuilderData.TEXT_BEFORE}", translator.translate(prompt.textBefore))
        variableParser.setVariable(
            "prompt.${PromptBuilderData.SELECTED_TEXT}",
            translator.translate(prompt.selectedText)
        )
        variableParser.setVariable("tokens", tokens.toString())
        variableParser.setVariable(
            "stop", stop.joinToString(
                separator = "\",\"",
                prefix = "\"",
                postfix = "\"",
            ) { translator.translate(it) }
        )

        val request = Request.Builder()
            .url(variableParser.parse(url))
            .apply {
                headers.forEach {
                    addHeader(variableParser.parse(it.first), variableParser.parse(it.second))
                }
            }
            .method(
                method,
                if (method == "POST") variableParser.parse(bodyTemplate).toRequestBody(mediaType)
                else null
            )
            .build()

        val call = client.newCall(request)
        val response = runAsync {
            call.runCatching { execute() }
                .onFailure {
                    lastTimeWhenInvoked = System.currentTimeMillis()
                    return@runAsync when (it) {
                        is SocketTimeoutException -> Result.failure(
                            ResponseTimeoutError(url, client.connectTimeoutMillis / 1000f)
                        )

                        is UnknownHostException -> Result.failure(ResponseUnknownHostError(url))
                        is ConnectionShutdownException -> Result.failure(ResponseConnectionShutdownError(url))
                        is IOException -> Result.failure(ResponseServerUnavailableError(url))
                        else -> Result.failure(it)
                    }
                }.getOrThrow()
                .let {
                    lastTimeWhenInvoked = System.currentTimeMillis()
                    return@runAsync if (!it.isSuccessful)
                        Result.failure(ResponseFailedError(url, it.code, it.body?.string() ?: ""))
                    else
                        parser.parseBody(it.body?.string() ?: "")
                }
        }

        while (response.isPending)
            withContext(NonCancellable) {
                if (!isActive) {
                    call.cancel()
                    return@withContext Result.failure<String>(Error("Cancelled"))
                }
                delay(50)
            }

        return response.await()
    }

    override val delayTime: Long
        get() = minimumDelayBetweenRequestsInMillis.toLong()

}