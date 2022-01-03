package com.github.tomislaw.pickyourautocompletion.settings.data.integrations

import com.github.tomislaw.pickyourautocompletion.autocompletion.predictor.webhook.parser.BodyParser

data class WebhookRequestData(
    val maxSize: Int,
    val method: String,
    val url: String,
    val headers: Map<String, String>,
    val bodyTemplate: String,
    val bodyParser: BodyParser,
    val timeout: Int = 30
)