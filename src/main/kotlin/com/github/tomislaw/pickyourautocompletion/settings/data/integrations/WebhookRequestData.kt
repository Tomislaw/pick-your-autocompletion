package com.github.tomislaw.pickyourautocompletion.settings.data.integrations

import com.github.tomislaw.pickyourautocompletion.autocompletion.predictor.webhook.parser.BodyParser

data class WebhookRequestData(
    var maxSize: Int = 2048,
    var method: String = "POST",
    var url: String = "",
    var headers: MutableList<Pair<String, String>> =  mutableListOf(),
    var bodyTemplate: String = "",
    var bodyParser: BodyParser? = null,
    var timeout: Int = 30
)