package com.github.tomislaw.pickyourautocompletion.autocompletion.predictor

interface Predictor {
    fun predict(codeContext: String): String
}