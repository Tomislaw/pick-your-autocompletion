package com.github.tomislaw.pickyourautocompletion.errors

import java.util.*

class MissingConfigurationError : Exception(
    ResourceBundle.getBundle("messages.ErrorMessages", Locale.getDefault())
        .getString("missingConfigurationError")
        .format()
)