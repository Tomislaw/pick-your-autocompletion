package com.github.tomislaw.pickyourautocompletion.settings.data.integrations

import com.github.tomislaw.pickyourautocompletion.autocompletion.predictor.webhook.parser.JsonBodyParser
import com.github.tomislaw.pickyourautocompletion.settings.data.EntryPoint

class OpenAiIntegration(
    override val order: Int = 0,
    override val supportedFiles: Collection<String> = emptyList(),
    override val promptBuilder: String = "default",
    override val name: String,
    val apiKey: String,
    val engine: String
) : EntryPoint {
    val request: WebhookRequestData
        get() = WebhookRequestData(
            maxSize = 2048,
            method = "POST",
            headers = mapOf(Pair("Authorization", "Bearer $apiKey")),
            bodyTemplate = "{\n" +
                    "  \"prompt\": \"\${body}\",\n" +
                    "  \"max_tokens\": \${tokens},\n" +
                    "  \"temperature\": 1,\n" +
                    "  \"top_p\": 0.5,\n" +
                    "  \"n\": 1,\n" +
                    "  \"stream\": false,\n" +
                    "  \"logprobs\": null,\n" +
                    "  \"stop\": \"\${stop}" +
                    "}",
            bodyParser = JsonBodyParser("/choices/0/text"),
            url = "https://api.openai.com/v1/$engine/completions"
        )
    override val type = EntryPoint.OPENAI

}