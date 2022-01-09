package com.github.tomislaw.pickyourautocompletion.autocompletion.predictor

import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.rd.util.toPromise
import com.intellij.openapi.util.TextRange
import com.intellij.psi.*
import kotlinx.coroutines.*
import java.util.concurrent.TimeUnit
import javax.xml.bind.JAXBElement


@Suppress("UnstableApiUsage")
class PredictModeProvider(
    private val inlineChars: List<Char> = listOf(']', ')'),
) {

    @OptIn(DelicateCoroutinesApi::class, ExperimentalCoroutinesApi::class)
    fun getPredictionMode(
        offset: Int,
        editor: Editor,
        project: Project,
        additionalStopList: List<String> = listOf("\n\n")
    ): Pair<PredictMode, List<String>> =
        ReadAction.compute<Pair<PredictMode, List<String>>, Throwable> {

//            val sameEditor = GlobalScope.async(Dispatchers.EDT) {
//                editor == FileEditorManager.getInstance(project).selectedEditors.map { it.file }.contains(editor.fi)
//            }.toPromise().blockingGet(200, TimeUnit.MILLISECONDS) ?: false

            // don't predict if out of scope
            if (!editor.document.isWritable
//                || !sameEditor
                || editor !is EditorImpl
                || offset < 0
                || offset > editor.document.textLength
            )
                return@compute Pair(PredictMode.NONE, emptyList())

            val lineNumber = editor.document.getLineNumber(offset)
            val lineEnd = editor.document.getLineEndOffset(lineNumber)
            val lineText = editor.document.getText(TextRange(offset, lineEnd))

            for (char in lineText) {
                when {
                    char.isWhitespace() -> continue
                    // if ending with bracket then make short prediction
                    inlineChars.contains(char) -> return@compute Pair(
                        PredictMode.ONE_LINE,
                        listOf("$char", "\n") + additionalStopList
                    )
                    // if surrounded by characters do not predict anything
                    else -> return@compute Pair(
                        PredictMode.NONE,
                        emptyList()
                    )
                }
            }

            val element = editor.project?.let {
                PsiManager.getInstance(it).findFile(editor.virtualFile)?.findElementAt(offset)
            }

            if (element != null) {
                // if it is comment then make short prediction
                if (element is PsiComment)
                    return@compute Pair(
                        PredictMode.ONE_LINE,
                        listOf("\n") + additionalStopList
                    )
            }

            // make long prediction
            return@compute Pair(PredictMode.MULTILINE, additionalStopList)
        }

    enum class PredictMode {
        MULTILINE,
        ONE_LINE,
        NONE
    }
}