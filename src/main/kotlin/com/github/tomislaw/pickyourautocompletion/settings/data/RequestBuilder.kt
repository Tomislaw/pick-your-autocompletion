package com.github.tomislaw.pickyourautocompletion.settings.data

import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.webhook.parser.BodyParser
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.webhook.parser.JsonBodyParser
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.webhook.parser.RegexBodyParser
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.webhook.parser.XmlBodyParser
import java.nio.charset.Charset

data class RequestBuilder(
    val maxSize: Int = 2048,
    val method: String = "POST",
    val url: String = "",
    val headers: MutableList<Pair<String, String>> = mutableListOf(),
    val bodyTemplate: String = "",
    val bodyParserType: String = "",
    val bodyParserData: String = "",
    val timeoutInMillis: Int = 0,
    val minimumDelayBetweenRequestsInMillis: Int = 0,
    val contentType: String = "application/json",
    val charset: Charset = Charsets.UTF_8
) {
    val bodyParser: BodyParser?
        get() = when (bodyParserType) {
            "From Xml" -> XmlBodyParser(bodyParserData)
            "From json" -> JsonBodyParser(bodyParserData)
            "Regex" -> RegexBodyParser(bodyParserData)
            else -> null
        }

    val isConfigured: Boolean
        get() = url.isNotEmpty()

    companion object {
        fun openAi(apiKey: String) = RequestBuilder(
            maxSize = 2048,
            method = "POST",
            url = "https://api.openai.com/v1/completions",
            headers = mutableListOf(Pair("Authorization", "Bearer $apiKey")),
            bodyTemplate = "{\n" +
                    "  \"model\": \"code-cushman-001\",\n" +
                    "  \"prompt\": \"\${body}\",\n" +
                    "  \"max_tokens\": 100,\n" +
                    "  \"temperature\": 1,\n" +
                    "  \"top_p\": 0.5,\n" +
                    "  \"n\": 1,\n" +
                    "  \"stream\": false,\n" +
                    "  \"logprobs\": null,\n" +
                    "  \"stop\": \${stop}\n" +
                    "}",
            bodyParserType = "From json",
            bodyParserData = "/choices/0/text",
            timeoutInMillis = 5000,
            minimumDelayBetweenRequestsInMillis = 2500
        )

        fun huggingface(apiKey: String) = RequestBuilder(
            maxSize = 2048,
            method = "POST",
            url = "https://api-inference.huggingface.co/models/EleutherAI/gpt-neo-2.7B",
            headers = mutableListOf(Pair("Authorization", "Bearer $apiKey")),
            bodyTemplate = "{\n" +
                    "  \"inputs\": \"\${body}\",\n" +
                    "  \"parameters\" : {\n" +
                    "    \"max_new_tokens\": 100,\n" +
                    "    \"temperature\": 1,\n" +
                    "    \"top_p\": 0.5,\n" +
                    "    \"num_return_sequences\": 1,\n" +
                    "    \"return_full_text\": false\n" +
                    "  }\n" +
                    "}",
            bodyParserType = "From json",
            bodyParserData = "/0/generated_text",
            timeoutInMillis = 30000,
            minimumDelayBetweenRequestsInMillis = 3000
        )
    }
}