package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.webhook.parser

import com.fasterxml.jackson.dataformat.xml.XmlMapper
import com.github.tomislaw.pickyourautocompletion.errors.ResponseParsingError

class XmlBodyParser(val path: String) : BodyParser {
    private var mapper = XmlMapper()
    override fun parseBody(body: String): Result<String> =
        mapper.readTree(body)
            .at(path)
            .runCatching { textValue() }
            .onFailure {
                return Result.failure(ResponseParsingError(path, body))
            }
}