package com.github.tomislaw.pickyourautocompletion.errors

import java.util.*

class ResponseConnectionShutdownError(hostname: String) : Exception(
    ResourceBundle.getBundle("messages.ErrorMessages", Locale.getDefault())
        .getString("responseConnectionShutdownError")
        .format(hostname)
)