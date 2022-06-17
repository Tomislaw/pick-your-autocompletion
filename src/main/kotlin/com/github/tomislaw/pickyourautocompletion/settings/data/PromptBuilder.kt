package com.github.tomislaw.pickyourautocompletion.settings.data

import com.github.tomislaw.pickyourautocompletion.utils.Base64StringSerializer
import com.intellij.util.xmlb.annotations.OptionTag

data class PromptBuilder(
    @OptionTag(converter = Base64StringSerializer::class)
    var template: String = "",
    var maxSize: Int = 1024
) {
    companion object {
        fun default() = PromptBuilder(
            "Language: \${language}\n" +
                    "File: \${directory}\${file}\n" +
                    "###\n" +
                    "\${text}", 1024
        )
    }
}


