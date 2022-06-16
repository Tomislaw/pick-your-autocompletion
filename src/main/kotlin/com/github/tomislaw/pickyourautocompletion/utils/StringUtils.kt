package com.github.tomislaw.pickyourautocompletion.utils

fun String.firstLine(): String {
    val index = this.indexOf("\n", 1)
    return if (index == -1) this else this.substring(0, index)
}