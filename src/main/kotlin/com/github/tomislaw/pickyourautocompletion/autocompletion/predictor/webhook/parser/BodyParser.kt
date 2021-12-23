package com.github.tomislaw.pickyourautocompletion.autocompletion.predictor.webhook.parser

interface BodyParser {
    fun parseBody(body: String) : String
}