package com.github.tomislaw.pickyourautocompletion.errors

import java.util.*

class ResponseTimeoutError(hostname: String, timeoutTime: Float) : Exception(
    ResourceBundle.getBundle("ErrorMessages", Locale.getDefault()).getString("responseTimeoutError")
        .format(hostname, timeoutTime)
)