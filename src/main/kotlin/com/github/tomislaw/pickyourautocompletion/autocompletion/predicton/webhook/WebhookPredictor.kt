package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.webhook

import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.Predictor
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.webhook.parser.BodyParser
import com.github.tomislaw.pickyourautocompletion.autocompletion.template.VariableTemplateParser
import com.github.tomislaw.pickyourautocompletion.settings.data.RequestBuilder
import okhttp3.MediaType
import okhttp3.MediaType.Companion.toMediaType
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import org.apache.commons.text.StringEscapeUtils
import org.apache.commons.text.translate.CharSequenceTranslator
import java.nio.charset.Charset
import java.time.Duration

class WebhookPredictor(request: RequestBuilder) : Predictor {
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
            .connectTimeout(Duration.ofSeconds(request.timeout.toLong()))
            .build()
        parser = request.bodyParser
        bodyTemplate = request.bodyTemplate
        charset = request.charset
        mediaType = request.contentType.runCatching { toMediaType() }.getOrNull()
        url = request.url
        headers = request.headers
        method = request.method

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

        return client.newCall(request).execute().let {
            val body = it.body?.string() ?: ""
            parser?.parseBody(body) ?: body
        }
    }
}