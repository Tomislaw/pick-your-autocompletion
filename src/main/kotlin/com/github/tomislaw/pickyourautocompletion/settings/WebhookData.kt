package com.github.tomislaw.pickyourautocompletion.settings

data class WebhookData(
    val request: WebhookRequestData,
    val responseParser: String
)

data class WebhookRequestData(
    val maxSize: Int,
    val method: String,
    val headers: List<String>,
    val bodyTemplate: String
)
