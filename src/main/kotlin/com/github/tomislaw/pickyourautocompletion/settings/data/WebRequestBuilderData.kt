package com.github.tomislaw.pickyourautocompletion.settings.data

import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.webhook.parser.BodyParser
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.webhook.parser.JsonBodyParser
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.webhook.parser.RegexBodyParser
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.webhook.parser.XmlBodyParser
import com.github.tomislaw.pickyourautocompletion.utils.Base64HeaderListSerializer
import com.github.tomislaw.pickyourautocompletion.utils.Base64StringSerializer
import com.github.tomislaw.pickyourautocompletion.utils.CharsetSerializer
import com.github.tomislaw.pickyourautocompletion.utils.MyProperties
import com.intellij.util.xmlb.annotations.OptionTag
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.nio.charset.Charset
import java.util.*

data class WebRequestBuilderData(
    var maxSize: Int = 2048,
    var method: String = "POST",
    var url: String = "",
    @OptionTag(converter = Base64HeaderListSerializer::class)
    var headers: MutableList<Pair<String, String>> = mutableListOf(),
    @OptionTag(converter = Base64StringSerializer::class)
    var bodyTemplate: String = "",
    var bodyParserType: String = "",
    @OptionTag(converter = Base64StringSerializer::class)
    var bodyParserData: String = "",
    var timeoutInMillis: Int = 0,
    var minimumDelayInMillis: Int = 0,
    var contentType: String = "application/json",
    @OptionTag(converter = CharsetSerializer::class)
    var charset: Charset = Charsets.UTF_8
) {
    val bodyParser: BodyParser?
        get() = when (bodyParserType) {
            "From Xml" -> XmlBodyParser(bodyParserData)
            "From json" -> JsonBodyParser(bodyParserData)
            "Regex" -> RegexBodyParser(bodyParserData)
            else -> null
        }

    val isConfigured: Boolean
        get() = url.isNotEmpty()

    companion object {

        private val properties = MyProperties("bundles.WebBuilders")

        fun fromProperties(builder: String, apiKey: String) = WebRequestBuilderData(
            maxSize = properties.property(builder, "maxSize")!!.toInt(),
            method = properties.property(builder, "method")!!,
            url = properties.property(builder, "url")!!,
            headers = mutableListOf(Pair("Authorization", "Bearer $apiKey")),
            bodyTemplate = properties.property(builder, "bodyTemplate")!!,
            bodyParserType = properties.property(builder, "bodyParserType")!!,
            bodyParserData = properties.property(builder, "bodyParserData")!!,
            timeoutInMillis = properties.property(builder, "timeoutInMillis")!!.toInt(),
            minimumDelayInMillis = properties.property(builder, "minimumDelayInMillis")!!.toInt(),
        )

        fun validatorFromProperties(builder: String, apiKey: String) =
            Request.Builder()
                .url(properties.property(builder, "url")!!)
                .addHeader("Authorization", "Bearer $apiKey")
                .method(
                    properties.property(builder, "method")!!,
                    properties.property(builder, "bodyTemplate")?.toRequestBody()
                )
                .build()
    }

}