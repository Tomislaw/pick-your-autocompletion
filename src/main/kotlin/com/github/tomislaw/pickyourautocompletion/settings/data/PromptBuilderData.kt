package com.github.tomislaw.pickyourautocompletion.settings.data

import com.github.tomislaw.pickyourautocompletion.autocompletion.template.VariableTemplateParser
import com.github.tomislaw.pickyourautocompletion.utils.Base64StringSerializer
import com.github.tomislaw.pickyourautocompletion.utils.MyProperties
import com.intellij.util.xmlb.annotations.OptionTag

data class PromptBuilderData(
    @OptionTag(converter = Base64StringSerializer::class) var template: String = "",
    var maxSize: Int = 1024,
    var maxTextBeforeSize: Int = maxSize,
    var maxTextAfterSize: Int = maxSize,
    var multiFileEnabled: Boolean = true
) {

    val templateSize: Int by lazy {
        VariableTemplateParser().parse(template).length
    }

    val containsFile: Boolean by lazy {
        template.contains("\${$FILE}")
    }

    val containsLanguage: Boolean by lazy {
        template.contains("\${$LANGUAGE}")
    }

    val containsDirectory: Boolean by lazy {
        template.contains("\${$DIRECTORY}")
    }

    val containsTextBefore: Boolean by lazy {
        template.contains("\${$TEXT_BEFORE}")
    }

    val containsTextAfter: Boolean by lazy {
        template.contains("\${$TEXT_AFTER}")
    }

    val isConfigured: Boolean
        get() = template.isNotEmpty()

    companion object {
        const val LANGUAGE = "language"
        const val DIRECTORY = "directory"
        const val FILE = "file"
        const val TEXT_BEFORE = "textBefore"
        const val TEXT_AFTER = "textAfter"
        const val SELECTED_TEXT = "selectedText"
        private val properties = MyProperties("bundles.Prompts")

        fun fromProperties(builder: String) = PromptBuilderData(
            maxSize = properties.property(builder, "maxPromptSize")!!.toInt(),
            maxTextBeforeSize = properties.property(builder, "maxTextBeforeSize")!!.toInt(),
            maxTextAfterSize = properties.property(builder, "maxTextAfterSize")!!.toInt(),
            template = properties.property(builder, "prompt")!!,
            multiFileEnabled = properties.property(builder, "useMultiFiles")!!.toBooleanStrictOrNull() ?: false,
        )
    }
}


