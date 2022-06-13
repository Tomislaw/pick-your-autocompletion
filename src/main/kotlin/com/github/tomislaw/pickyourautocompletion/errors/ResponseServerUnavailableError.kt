package com.github.tomislaw.pickyourautocompletion.errors

import java.util.*

class ResponseServerUnavailableError (hostname: String) : Exception(
    ResourceBundle.getBundle("messages,ErrorMessages", Locale.getDefault())
        .getString("responseFailedError")
        .format(hostname)
)