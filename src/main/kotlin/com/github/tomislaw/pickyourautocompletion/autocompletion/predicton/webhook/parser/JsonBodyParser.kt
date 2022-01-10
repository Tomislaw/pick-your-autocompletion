package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.webhook.parser

import com.fasterxml.jackson.databind.ObjectMapper


class JsonBodyParser(val path: String) : BodyParser {
    private var mapper = ObjectMapper()
    override fun parseBody(body: String): String {
        println(body)
        return mapper.readTree(body).at(path).textValue()
    }
}