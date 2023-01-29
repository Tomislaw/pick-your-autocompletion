package com.github.tomislaw.pickyourautocompletion.errors

import com.github.tomislaw.pickyourautocompletion.localizedText
import com.github.tomislaw.pickyourautocompletion.settings.configurable.SettingsConfigurable

class MissingConfigurationError : ShowConfigError, Exception(
    localizedText("error.missingConfiguration").format()
)

interface ShowConfigError {
    val configClass: Class<SettingsConfigurable>
        get() = SettingsConfigurable::class.java
}
