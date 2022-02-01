package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.editor.Editor

class PredictionSanitizer(
    private val bracketMap: Map<Char, Char> = mapOf(
        Pair('{', '}'),
        Pair('(', ')'),
        Pair('<', '>'),
        Pair('[', ']'),
    )
) {
    // todo, find better way for doing it
    // remove unneeded brackets from prediction, trim until stop sign occurred
    fun sanitize(editor: Editor, offset: Int, code: String, stopList: Collection<String>): String {
        var stopIndex = code.length

        val bracketCounter: MutableMap<Char, Int> = mutableMapOf()
        for (bracket in bracketMap)
            bracketCounter[bracket.value] = 0

        // calculate number of unopened brackets to know how much of them we need to trim
        val editorText = ReadAction.compute<String, Throwable> { editor.document.text }
        for (index in editorText.indices) {
            val c = editorText[index]
            when {
                bracketMap.containsKey(c) -> {
                    bracketCounter[bracketMap[c]!!] = bracketCounter[bracketMap[c]!!]!! + 1
                }
                bracketCounter.containsKey(c) -> {
                    if (bracketCounter[c]!! > 0)
                        bracketCounter[c] = bracketCounter[c]!! - 1
                    else if (index > offset) // if too many closing brackets then don't bother
                    {
                        bracketCounter.clear()
                        break;
                    }
                }
            }
        }

        for (i in code.indices) {

            // if reached stop phrase then trim content
            for (stop in stopList) {
                if (code.regionMatches(i, stop, 0, stop.length)) {
                    stopIndex = i
                    break
                }
            }

            // ignoring bracket check
            if(bracketCounter.isEmpty())
                continue

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
        }

        return code.substring(0, stopIndex).trimEnd()
    }
}