package com.github.tomislaw.pickyourautocompletion

import java.util.*

fun localizedText(property: String): String =
    ResourceBundle.getBundle("bundles.Texts", Locale.getDefault()).getString(property)



