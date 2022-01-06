package com.github.tomislaw.pickyourautocompletion.utils


fun String.withUniqueName(names: Collection<String>): String {
    var nextName = this
    while (names.contains(nextName))
        nextName += " new"
    return nextName
}