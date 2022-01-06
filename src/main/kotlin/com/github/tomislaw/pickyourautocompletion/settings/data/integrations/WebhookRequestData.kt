package com.github.tomislaw.pickyourautocompletion.settings.data.integrations

import com.github.tomislaw.pickyourautocompletion.autocompletion.predictor.webhook.parser.BodyParser
import okhttp3.MediaType
import java.nio.charset.Charset

data class WebhookRequestData(
    var maxSize: Int = 2048,
    var method: String = "POST",
    var url: String = "",
    var headers: MutableList<Pair<String, String>> =  mutableListOf(),
    var bodyTemplate: String = "",
    var bodyParser: BodyParser? = null,
    var timeout: Int = 30,
    var contentType: String = "application/json",
    var charset: Charset = Charsets.UTF_8
)