package com.github.tomislaw.pickyourautocompletion.autocompletion.predictor.webhook

import com.github.tomislaw.pickyourautocompletion.autocompletion.predictor.Predictor
import com.github.tomislaw.pickyourautocompletion.autocompletion.template.TemplateParser

open class WebhookCodePredictor : Predictor {



    override fun predict(codeContext: String): String {
        return "a"
    }
}