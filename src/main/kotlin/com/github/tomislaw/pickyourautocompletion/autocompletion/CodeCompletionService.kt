package com.github.tomislaw.pickyourautocompletion.autocompletion

import com.github.tomislaw.pickyourautocompletion.autocompletion.predictor.Predictor
import com.intellij.openapi.application.ApplicationManager

class CodeCompletionService {
    val list: List<Predictor>
    init {
        list = ArrayList()
    }

    companion object {
        val instance: CodeCompletionService
            get() = ApplicationManager.getApplication().getService(CodeCompletionService::class.java)
    }
}