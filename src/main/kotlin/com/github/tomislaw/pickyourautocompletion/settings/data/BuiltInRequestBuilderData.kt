package com.github.tomislaw.pickyourautocompletion.settings.data

import com.github.tomislaw.pickyourautocompletion.utils.Base64StringSerializer
import com.github.tomislaw.pickyourautocompletion.utils.MyProperties
import com.intellij.util.xmlb.annotations.OptionTag

data class BuiltInRequestBuilderData(
    @OptionTag(converter = Base64StringSerializer::class)
    var modelLocation: String = "",

    @OptionTag(converter = Base64StringSerializer::class)
    var tokenizerLocation: String = "",

    var stopSequences: List<String> = listOf(),

    var topK: Int = 5,
    var topP: Float = 1f,
    var temperature: Float = 1f,
    var device: Int = 0,

    var inputOutput: InputsOutputs = InputsOutputs(),

    var maxTokens: Int = 100

) {
    val isConfigured: Boolean
        get() = modelLocation.isNotEmpty() && tokenizerLocation.isNotEmpty()


    data class InputsOutputs(
        var inputIds: String = "",
        var attentionMask: String = "",
        var logits: String = "",
        var cache: Map<String, String> = mapOf()
    )

    companion object {
        val properties = MyProperties("bundles.BuiltInBuilders")

        fun fromProperties(builder: String, model: String, tokenizer: String, device: Int = 0) =
            BuiltInRequestBuilderData(
                topK = properties.property(builder, "topK")!!.toInt(),
                topP = properties.property(builder, "topP")!!.toFloat(),
                temperature = properties.property(builder, "temperature")!!.toFloat(),
                modelLocation = model,
                tokenizerLocation = tokenizer,
                stopSequences = properties.propertyArray(builder, "stopSequence"),
                device = device,
                inputOutput = InputsOutputs(
                    inputIds = properties.property(builder, "input.inputIds")!!,
                    attentionMask = properties.property(builder, "input.attentionMasks")!!,
                    logits = properties.property(builder, "output.logits")!!,
                    cache = properties.propertyArray(builder, "cache").associate {
                        val values = it.split(",")
                        Pair(values[0], values[1])
                    },
                )
            )
    }
}