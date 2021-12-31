package com.github.tomislaw.pickyourautocompletion.autocompletion.predictor.webhook

import com.fasterxml.jackson.core.io.JsonStringEncoder
import com.github.tomislaw.pickyourautocompletion.autocompletion.predictor.Predictor
import com.github.tomislaw.pickyourautocompletion.autocompletion.predictor.webhook.parser.BodyParser
import com.github.tomislaw.pickyourautocompletion.autocompletion.predictor.webhook.parser.JsonBodyParser
import com.github.tomislaw.pickyourautocompletion.autocompletion.template.TemplateParser
import com.github.tomislaw.pickyourautocompletion.autocompletion.template.VariableTemplateParser
import com.intellij.util.io.HttpRequests
import org.apache.commons.text.StringEscapeUtils
import org.apache.http.HttpEntity
import org.apache.http.client.fluent.Request
import org.apache.http.entity.ContentType
import java.net.URI
import java.net.URL
import java.nio.charset.Charset

open class WebhookCodePredictor(
    private val method: Method,
    private val uri: String,
    private val headers: List<Pair<String, String>>,
    private val bodyTemplate: String,
    private val bodyParser: BodyParser,
    private val charset: Charset = Charset.defaultCharset(),
    private val connectionTimeout: Int = 30000,
    private val socketTimeout: Int = 30000,
) : Predictor {

    private val templateParser = VariableTemplateParser()

    override fun predict(codeContext: String, tokens: Int, stop: List<String>): String {
        templateParser.setVariable("body", codeContext)

        templateParser.setVariable("tokens", tokens.toString())

        val ahh = 0
        val body = when (ahh) {
            0 -> {
                templateParser.setVariable("stop", stop.joinToString(
                    separator = ",",
                    transform = { t -> StringEscapeUtils.ESCAPE_JSON.translate(t) }
                ))
                StringEscapeUtils.ESCAPE_JSON.translate(codeContext)
            }
            1 -> {
                templateParser.setVariable("stop", stop.joinToString(
                    separator = ",",
                    transform = { t -> StringEscapeUtils.ESCAPE_XML11.translate(t) }
                ))
                StringEscapeUtils.ESCAPE_XML11.translate(codeContext)
            }
            else -> {
                templateParser.setVariable(
                    "stop", stop.joinToString(
                        separator = ",",
                    )
                )
                codeContext
            }
        }.let {
            templateParser.setVariable("body", it)
            templateParser.parse(bodyTemplate)
        }

        val response = when (method) {
            Method.POST -> Request.Post(uri).bodyString(
                body,
                ContentType.APPLICATION_JSON
            )
            Method.GET -> Request.Get(uri)
        }.apply {
            headers.forEach { header ->
                this.addHeader(
                    templateParser.parse(header.first),
                    templateParser.parse(header.second)
                )
            }
        }.connectTimeout(connectionTimeout)
            .socketTimeout(socketTimeout)
            .execute().returnContent().let { content -> bodyParser.parseBody(content.asString(charset)) }
        templateParser.removeVariable("body")
        return response
    }

    enum class Method { POST, GET }

    companion object {
        val DEFAULT: WebhookCodePredictor = WebhookCodePredictor(
            Method.POST,
            "https://api.openai.com/v1/engines/cushman-codex/completions",
            listOf(Pair("Authorization", "Bearer APIKEY")),
            "{\n" +
                    "  \"prompt\": \"\${body}\",\n" +
                    "  \"max_tokens\": 50,\n" +
                    "  \"temperature\": 1,\n" +
                    "  \"top_p\": 0.5,\n" +
                    "  \"n\": 1,\n" +
                    "  \"stream\": false,\n" +
                    "  \"logprobs\": null,\n" +
                    "  \"stop\": \"\${stop}\"" +
                    "}",
            JsonBodyParser("/choices/0/text")
        )
    }
}