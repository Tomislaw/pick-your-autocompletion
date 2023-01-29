package com.github.tomislaw.pickyourautocompletion.settings.data

import com.github.tomislaw.pickyourautocompletion.utils.MyProperties

data class PredictionSanitizerData(
    var smartStopTokens: Boolean = true,
    var maxLines: Int = 4,
    var additionalStopTokens: List<String> = listOf()
) {

    companion object {

        private val properties = MyProperties("bundles.Settings")

        fun fromProperties(builder: String) = PredictionSanitizerData(
            smartStopTokens = properties.property(builder, "smartStopTokens")!!.toBooleanStrict(),
            maxLines = properties.property(builder, "maxLines")!!.toInt(),
            additionalStopTokens = properties.property(builder, "stopTokens")!!.split(",")
        )
    }
}