package com.github.tomislaw.pickyourautocompletion.settings.data.integrations

import com.github.tomislaw.pickyourautocompletion.autocompletion.predictor.webhook.WebhookCodePredictor
import com.github.tomislaw.pickyourautocompletion.autocompletion.predictor.webhook.parser.JsonBodyParser
import com.github.tomislaw.pickyourautocompletion.settings.data.ApiKey
import com.github.tomislaw.pickyourautocompletion.settings.data.EntryPoint

data class WebhookIntegration(
    var request: WebhookRequestData = WebhookRequestData(),
    override var order: Int = 0,
    override var supportedFiles: Collection<String> = emptyList(),
    override var promptBuilder: String = "",
    override var name: String = "",
) : EntryPoint {
    override val type = EntryPoint.WEBHOOK

    companion object {
        fun openAi(apiKey: ApiKey): WebhookIntegration = WebhookIntegration(
            request = WebhookRequestData(
                maxSize = 2048,
                method = "POST",
                url = "https://api.openai.com/v1/engines/cushman-codex/completions",
                headers = mutableListOf(Pair("Authorization", "Bearer \${pwd.${apiKey.name}}")),
                bodyTemplate = "{\n" +
                        "  \"prompt\": \"\${body}\",\n" +
                        "  \"max_tokens\": 50,\n" +
                        "  \"temperature\": 1,\n" +
                        "  \"top_p\": 0.5,\n" +
                        "  \"n\": 1,\n" +
                        "  \"stream\": false,\n" +
                        "  \"logprobs\": null,\n" +
                        "  \"stop\": \${stop}\n" +
                        "}",
                bodyParser = JsonBodyParser("/choices/0/text"),
                timeout = 30
            )
        )

        fun huggingface(apiKey: ApiKey): WebhookIntegration = WebhookIntegration(
            request = WebhookRequestData(
                maxSize = 2048,
                method = "POST",
                url = "https://api.openai.com/v1/engines/cushman-codex/completions",
                headers = mutableListOf(Pair("Authorization", "Bearer \${pwd.${apiKey.name}}")),
                bodyTemplate = "{\n" +
                        "  \"prompt\": \"\${body}\",\n" +
                        "  \"max_tokens\": 50,\n" +
                        "  \"temperature\": 1,\n" +
                        "  \"top_p\": 0.5,\n" +
                        "  \"n\": 1,\n" +
                        "  \"stream\": false,\n" +
                        "  \"logprobs\": null,\n" +
                        "  \"stop\": \${stop}\n" +
                        "}",
                bodyParser = JsonBodyParser("/choices/0/text"),
                timeout = 30
            )
        )
    }

}