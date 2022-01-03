package com.github.tomislaw.pickyourautocompletion.settings.data.integrations

import com.github.tomislaw.pickyourautocompletion.settings.data.EntryPoint

data class WebhookIntegration(
    val request: WebhookRequestData,
    override val order: Int = 0,
    override val supportedFiles: Collection<String> = emptyList(),
    override val promptBuilder: String,
    override val name: String,
) : EntryPoint {
    override val type = EntryPoint.WEBHOOK
}

