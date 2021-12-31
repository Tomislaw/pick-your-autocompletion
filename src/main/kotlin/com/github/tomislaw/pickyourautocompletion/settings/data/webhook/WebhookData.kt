package com.github.tomislaw.pickyourautocompletion.settings.data.webhook

import com.github.tomislaw.pickyourautocompletion.autocompletion.predictor.webhook.parser.BodyParser
import com.github.tomislaw.pickyourautocompletion.autocompletion.predictor.webhook.parser.JsonBodyParser
import com.github.tomislaw.pickyourautocompletion.settings.data.EntryPoint

data class WebhookData(
    val request: WebhookRequestData,
    override val order: Int = 0,
    override val supportedFiles: Collection<String> = emptyList(),
    override val promptBuilder: String,
    override val name: String,
    override val type: String = EntryPoint.WEBHOOK,
) : EntryPoint {
    companion object {
        fun openAi(name: String, apiKey: String, engine: String): WebhookData = WebhookData(
            name = name,
            order = 0,
            request = WebhookRequestData(
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
                url = "https://api.openai.com/v1/$engine/cushman-codex/completions"
            ),
            promptBuilder = "default",
            type = EntryPoint.OPENAI
        )

    }
}

data class WebhookRequestData(
    val maxSize: Int,
    val method: String,
    val url: String,
    val headers: Map<String, String>,
    val bodyTemplate: String,
    val bodyParser: BodyParser
)

