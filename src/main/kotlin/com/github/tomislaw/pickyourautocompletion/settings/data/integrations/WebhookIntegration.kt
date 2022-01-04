package com.github.tomislaw.pickyourautocompletion.settings.data.integrations

import com.github.tomislaw.pickyourautocompletion.settings.data.EntryPoint

data class WebhookIntegration(
    var request: WebhookRequestData = WebhookRequestData(),
    override var order: Int = 0,
    override var supportedFiles: Collection<String> = emptyList(),
    override var promptBuilder: String = "",
    override var name: String = "",
) : EntryPoint {
    override val type = EntryPoint.WEBHOOK
}

