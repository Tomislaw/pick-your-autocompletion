package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.webhook.parser

import com.fasterxml.jackson.databind.ObjectMapper
import com.github.tomislaw.pickyourautocompletion.errors.ResponseParsingError

class JsonBodyParser(val path: String) : BodyParser {
    private var mapper = ObjectMapper()
    override fun parseBody(body: String): Result<String> =
        mapper.readTree(body)
            .at(path)
            .runCatching { textValue() }
            .onFailure {
                return Result.failure(ResponseParsingError(path, body))
            }
}