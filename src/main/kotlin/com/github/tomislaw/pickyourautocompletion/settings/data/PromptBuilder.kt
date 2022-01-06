package com.github.tomislaw.pickyourautocompletion.settings.data

data class PromptBuilder(
    val name: String,
    val template: String,
    val maxSize: Int
)