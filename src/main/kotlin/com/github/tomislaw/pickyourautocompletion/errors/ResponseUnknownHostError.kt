package com.github.tomislaw.pickyourautocompletion.errors

import java.util.*

class ResponseUnknownHostError(hostname: String) : Exception(
    ResourceBundle.getBundle("messages.ErrorMessages", Locale.getDefault())
        .getString("responseUnknownHostError")
        .format(hostname)
)