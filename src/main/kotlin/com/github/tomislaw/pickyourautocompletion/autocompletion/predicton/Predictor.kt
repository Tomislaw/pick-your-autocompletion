package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton

interface Predictor {

    val supportMultiple: Boolean get() = false
    suspend fun predictMultiple(
        codeContext: String,
        tokens: Int = 2048,
        stop: List<String> = listOf("\n\n"),
        count: Int
    ): Result<List<String>>{
        return Result.success(listOf())
    }

    suspend fun predict(codeContext: String, tokens: Int = 2048, stop: List<String> = listOf("\n\n")): Result<String>
    fun delayTime(): Long
}