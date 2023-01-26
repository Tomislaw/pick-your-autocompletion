package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton

import com.github.tomislaw.pickyourautocompletion.settings.data.PredictionSanitizerData
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiComment
import com.intellij.psi.PsiManager


class PredictionStopTokenProvider(
    private val data: PredictionSanitizerData,
    private val pairs: Map<Char, Char> = mapOf(
        Pair('{', '}'),
        Pair('(', ')'),
        Pair('<', '>'),
        Pair('>', '<'),
        Pair('[', ']'),
    ),
    private val checkCharacters: Int = 200
) {

    fun getPredictionMode(
        offset: Int,
        editor: Editor,
        additionalStopList: List<String> = listOf()
    ): Pair<PredictMode, List<String>> =
        ReadAction.compute<Pair<PredictMode, List<String>>, Throwable> {

            // don't predict if out of scope
            if (!editor.document.isWritable
                || editor !is EditorImpl
                || offset < 0
                || offset > editor.document.textLength
            ) {
                return@compute Pair(PredictMode.NOT_AVAILABLE, emptyList())
            }

            val lineNumber = editor.document.getLineNumber(offset)
            val lineEnd = editor.document.getLineEndOffset(lineNumber)
            val lineText = editor.document.getText(TextRange(offset, lineEnd))

            for (char in lineText) {
                when {
                    char.isWhitespace() -> continue
                    // if ending with bracket then make short prediction
                    pairs.values.contains(char) -> return@compute Pair(
                        PredictMode.DEFAULT,
                        listOf("$char", "\n") + additionalStopList
                    )
                    // if surrounded by characters do not predict anything
                    else -> return@compute Pair(
                        PredictMode.NOT_AVAILABLE,
                        emptyList()
                    )
                }
            }


            if (!data.contentAwareStopTokenEnabled) {
                val char = lineText.firstOrNull() ?: ' '
                if (char.isWhitespace() || pairs.values.contains(char))
                    return@compute Pair(PredictMode.DEFAULT, additionalStopList)
                else
                    return@compute Pair(PredictMode.NOT_AVAILABLE, additionalStopList)
            }

            val element = editor.project?.let {
                PsiManager.getInstance(it).findFile(editor.virtualFile)?.findElementAt(offset)
            }

            // if it is comment then make short prediction
            if (element is PsiComment) {
                return@compute Pair(
                    PredictMode.DEFAULT,
                    listOf("\n") + additionalStopList
                )
            }

            val openList = mutableSetOf<Char>()
            val closedList = mutableSetOf<Char>()

            // smart bracket stop token
            val checkText = editor.document.getText(
                TextRange(offset, (offset + checkCharacters).coerceAtMost(editor.document.textLength))
            )
            for (char in checkText) {
                when {
                    char.isWhitespace() -> continue
                    pairs.values.contains(char) -> closedList.add(char)
                    pairs.keys.contains(char) && !closedList.contains(pairs[char]) -> openList.add(char)
                }
            }

            val mappedOpenList = openList.map { pairs[it] }
            val stopChars = closedList.filter { !mappedOpenList.contains(it) }

            if (stopChars.isNotEmpty())
                return@compute Pair(
                    PredictMode.DEFAULT,
                    stopChars.map { it.toString() } + additionalStopList
                )


            return@compute Pair(PredictMode.DEFAULT, additionalStopList)
        }

    enum class PredictMode {
        DEFAULT,
        NOT_AVAILABLE
    }
}