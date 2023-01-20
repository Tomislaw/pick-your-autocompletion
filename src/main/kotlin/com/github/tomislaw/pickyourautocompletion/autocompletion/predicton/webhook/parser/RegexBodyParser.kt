package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.webhook.parser

class RegexBodyParser(regexString: String) : BodyParser {

    private val regex = Regex(regexString)
    override fun parseBody(body: String): Result<String> =
        regex.find(body)?.groups?.firstOrNull()?.value?.let { Result.success(it) } ?: Result.failure(
            Error(
                "Could not find any matching for $body when using regex ${regex.pattern}"
            )
        )
}
