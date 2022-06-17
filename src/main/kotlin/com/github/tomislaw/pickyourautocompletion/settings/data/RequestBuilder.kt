package com.github.tomislaw.pickyourautocompletion.settings.data

import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.webhook.parser.BodyParser
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.webhook.parser.JsonBodyParser
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.webhook.parser.RegexBodyParser
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.webhook.parser.XmlBodyParser
import com.github.tomislaw.pickyourautocompletion.utils.Base64HeaderListSerializer
import com.github.tomislaw.pickyourautocompletion.utils.Base64StringSerializer
import com.github.tomislaw.pickyourautocompletion.utils.CharsetSerializer
import com.intellij.util.xmlb.annotations.OptionTag
import java.nio.charset.Charset

data class RequestBuilder(
    var maxSize: Int = 2048,
    var method: String = "POST",
    var url: String = "",
    @OptionTag(converter = Base64HeaderListSerializer::class)
    var headers: MutableList<Pair<String, String>> = mutableListOf(),
    @OptionTag(converter = Base64StringSerializer::class)
    var bodyTemplate: String = "",
    var bodyParserType: String = "",
    @OptionTag(converter = Base64StringSerializer::class)
    var bodyParserData: String = "",
    var timeoutInMillis: Int = 0,
    var minimumDelayBetweenRequestsInMillis: Int = 0,
    var contentType: String = "application/json",
    @OptionTag(converter = CharsetSerializer::class)
    var charset: Charset = Charsets.UTF_8
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