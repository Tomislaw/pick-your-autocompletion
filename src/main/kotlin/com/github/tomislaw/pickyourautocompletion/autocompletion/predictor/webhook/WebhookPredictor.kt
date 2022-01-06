package com.github.tomislaw.pickyourautocompletion.autocompletion.predictor.webhook

import com.github.tomislaw.pickyourautocompletion.autocompletion.predictor.Predictor
import com.github.tomislaw.pickyourautocompletion.autocompletion.predictor.webhook.parser.BodyParser
import com.github.tomislaw.pickyourautocompletion.autocompletion.template.VariableTemplateParser
import com.github.tomislaw.pickyourautocompletion.settings.data.integrations.WebhookIntegration
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody
import okhttp3.RequestBody.Companion.toRequestBody
import okio.BufferedSink
import org.apache.commons.text.StringEscapeUtils
import org.apache.commons.text.translate.CharSequenceTranslator
import java.nio.charset.Charset
import java.time.Duration

class WebhookPredictor(data: WebhookIntegration) : Predictor {
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

    init {
        client = OkHttpClient.Builder()
            .connectTimeout(Duration.ofSeconds(data.request.timeout.toLong()))
            .build()
        parser = data.request.bodyParser
        bodyTemplate = data.request.bodyTemplate
        charset = data.request.charset
        mediaType = data.request.contentType.runCatching { toMediaType() }.getOrNull()
        url = data.request.url
        headers = data.request.headers
        method = data.request.method

        translator = StringEscapeUtils.ESCAPE_JSON
    }

    override fun predict(codeContext: String, tokens: Int, stop: List<String>): String {

        variableParser.setVariable("body", translator.translate(codeContext))
        variableParser.setVariable("tokens", tokens.toString())
        variableParser.setVariable(
            "stop", stop.map { translator.translate(it) }.joinToString(
                separator = "\",\"",
                prefix = "[\"",
                postfix = "\"]",
            )
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

        return client.newCall(request).execute().message.let {
            parser?.parseBody(it) ?: it
        }
    }
}