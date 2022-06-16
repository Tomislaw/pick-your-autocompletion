package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton

interface Predictor {
    suspend fun predict(codeContext: String, tokens: Int = 2048, stop: List<String> = listOf("\n\n")): Result<String>
    fun delayTime(): Long
}