package com.github.tomislaw.pickyourautocompletion.errors

import java.util.*

class ResponseFailedError(hostname: String, code: Int, body: String) : Exception(
    ResourceBundle.getBundle("messages.ErrorMessages", Locale.getDefault())
        .getString("responseNotSuccessfulError")
        .format(hostname, code, body)
)