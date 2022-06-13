package com.github.tomislaw.pickyourautocompletion.errors

import java.util.*

class ResponseParsingError(parserPath: String, response: String) : Exception(
    ResourceBundle.getBundle("messages.ErrorMessages", Locale.getDefault())
        .getString("responseParsingError")
        .format(parserPath, response)
)