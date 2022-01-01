package com.github.tomislaw.pickyourautocompletion.autocompletion

import com.github.tomislaw.pickyourautocompletion.autocompletion.predictor.Predictor
import com.intellij.openapi.application.ApplicationManager

class PredictorProviderService {
    val list: List<Predictor>
    init {
        list = ArrayList()
    }

    companion object {
        val instance: PredictorProviderService
            get() = ApplicationManager.getApplication().getService(PredictorProviderService::class.java)
    }
}