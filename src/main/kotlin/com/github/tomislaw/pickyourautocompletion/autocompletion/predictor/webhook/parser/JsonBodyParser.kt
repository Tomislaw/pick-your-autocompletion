package com.github.tomislaw.pickyourautocompletion.autocompletion.predictor.webhook.parser

import com.fasterxml.jackson.databind.ObjectMapper


class JsonBodyParser(private val property: String) : BodyParser {
    private var mapper = ObjectMapper()
    override fun parseBody(body: String): String {
        println(body)
        return mapper.readTree(body).at(property).textValue()
    }
}