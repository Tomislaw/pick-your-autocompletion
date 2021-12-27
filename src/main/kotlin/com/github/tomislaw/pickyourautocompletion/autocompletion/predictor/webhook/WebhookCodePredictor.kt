package com.github.tomislaw.pickyourautocompletion.autocompletion.predictor.webhook

import com.github.tomislaw.pickyourautocompletion.autocompletion.predictor.Predictor
import com.github.tomislaw.pickyourautocompletion.autocompletion.predictor.webhook.parser.BodyParser
import com.github.tomislaw.pickyourautocompletion.autocompletion.predictor.webhook.parser.JsonBodyParser
import com.github.tomislaw.pickyourautocompletion.autocompletion.template.TemplateParser
import com.github.tomislaw.pickyourautocompletion.autocompletion.template.VariableTemplateParser
import com.intellij.util.io.HttpRequests
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


    override fun predict(codeContext: String): String {

        templateParser.setVariable("body", codeContext)
        val body = templateParser.parse(bodyTemplate)
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
            .execute().returnContent().let { body -> bodyParser.parseBody(body.asString(charset)) }
        templateParser.removeVariable("body")
        return response
    }

    enum class Method { POST, GET }

    companion object {
        val DEFAULT: WebhookCodePredictor = WebhookCodePredictor(
            Method.POST,
            "https://api.openai.com/v1/engines/cushman-codex/completions",
            listOf(Pair("Authorization", "Bearer TOKEN")),
            "{\n" +
                    "  \"prompt\": \"\${body}\",\n" +
                    "  \"max_tokens\": 50,\n" +
                    "  \"temperature\": 1,\n" +
                    "  \"top_p\": 0.5,\n" +
                    "  \"n\": 1,\n" +
                    "  \"stream\": false,\n" +
                    "  \"logprobs\": null,\n" +
                    "  \"stop\": \"###\"" +
                    "}",
            JsonBodyParser("/choices/0/text")
        )
    }
}