package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton

class PredictionSanitizer(
    private val bracketMap: Map<Char, Char> = mapOf(
        Pair('{', '}'),
        Pair('(', ')'),
        Pair('<', '>'),
        Pair('[', ']'),
    )
) {

    fun sanitize(code: String, stopList: Collection<String>): String {
        var stopIndex = code.length

        val bracketCounter: MutableMap<Char, Int> = mutableMapOf()
        for (bracket in bracketMap)
            bracketCounter[bracket.value] = 0

        for (i in code.indices) {

            // if reached bracket pair then trim content
            if (bracketMap.keys.contains(code[i]))
                bracketCounter[bracketMap[code[i]]!!] = bracketCounter[bracketMap[code[i]]!!]!!.inc()
            else
                if (bracketCounter.containsKey(code[i])) {
                    bracketCounter[code[i]] = bracketCounter[code[i]]!!.minus(1)
                    if (bracketCounter[code[i]]!! < 0) {
                        stopIndex = i
                        break
                    }
                }

            for (stop in stopList) {
                // if reached stop phrase then trim content
                if (code.regionMatches(i, stop, 0, stop.length)) {
                    stopIndex = i
                    break
                }
            }
        }

        return code.substring(0, stopIndex).trimEnd()
    }
}