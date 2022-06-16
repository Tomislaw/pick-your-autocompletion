package com.github.tomislaw.pickyourautocompletion.settings.data

data class PromptBuilder(
    val template: String = "",
    val maxSize: Int = 1024
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


