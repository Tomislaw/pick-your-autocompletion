package com.github.tomislaw.pickyourautocompletion.settings.data.integrations

import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.webhook.parser.BodyParser
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
){
    fun deepCopy() : WebhookRequestData =
        copy(
            headers = mutableListOf<Pair<String, String>>().apply { addAll(headers.map { it.copy() }) },
        )
}