package com.github.tomislaw.pickyourautocompletion.utils

import java.util.*

class MyProperties(private val baseName: String) {

    fun property(builder: String, value: String) =
        runCatching {
            ResourceBundle.getBundle(baseName, Locale.getDefault()).getString("$builder.$value")
        }.getOrNull()

    fun propertyArray(builder: String, value: String) = runCatching {
        val results = mutableListOf<String>()
        var property: String?
        var index = 0
        do {
            val propertyString = "$value[$index]"
            property = property(builder, propertyString)?.apply {
                results.add(this)
            }
            index++
        } while (property != null)
        results
    }.getOrNull() ?: listOf()
}