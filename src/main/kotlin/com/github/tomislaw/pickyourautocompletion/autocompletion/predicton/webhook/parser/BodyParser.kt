package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.webhook.parser

interface BodyParser {
    fun parseBody(body: String) : String
}