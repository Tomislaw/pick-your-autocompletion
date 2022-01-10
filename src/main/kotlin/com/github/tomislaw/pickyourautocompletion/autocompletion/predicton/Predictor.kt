package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton

interface Predictor {
    fun predict(codeContext: String, tokens: Int = 2048, stop: List<String> = listOf("\n\n")): String
}