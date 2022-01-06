package com.github.tomislaw.pickyourautocompletion.autocompletion.predictor

interface Predictor {
    fun predict(codeContext: String, tokens: Int = 2048, stop: List<String> = listOf("\n\n")): String
}