package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton

import com.github.tomislaw.pickyourautocompletion.autocompletion.context.Prompt

interface Predictor {

    val supportMultiple: Boolean get() = false

    val delayTime: Long get() = 0
    suspend fun predictMultiple(
        prompt: Prompt, count: Int, tokens: Int = 100, stop: List<String> = listOf("\n\n"),
    ): Result<List<String>> {
        return Result.success(listOf())
    }

    suspend fun predict(prompt: Prompt, tokens: Int = 100, stop: List<String> = listOf("\n\n")): Result<String>

}