package com.github.tomislaw.pickyourautocompletion.autocompletion.predictor

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.psi.*

object SmartStopProvider {
    fun getStopString(offset: Int, editor: Editor): List<String> = ReadAction.compute<List<String>, Throwable> {

        if (editor !is EditorImpl || offset == 0 || offset > editor.document.textLength)
            return@compute listOf("\n")

        val project = editor.project ?: return@compute listOf("\n")

        val psi = PsiManager.getInstance(project).findFile(editor.virtualFile)
        var element = psi?.findElementAt(offset)

        return@compute when {
            element is PsiComment || element?.prevSibling is PsiComment -> listOf("\n")
            element?.prevSibling is PsiCodeFragment || element?.prevSibling?.prevSibling is PsiCodeFragment ->
                listOf("\n")
            else -> {
                for (i in 0..MAX_JUMPING_DISTANCE) {
                    if (element == null)
                        break;
                    if (element is PsiWhiteSpace) {
                        element = element.nextSibling
                        continue;
                    }
                    for (stop in STOP_LIST)
                        if (element.text?.startsWith(stop) == true)
                            return@compute listOf(stop, "\n\n", " \n")
                    return@compute listOf("\n")
                }
                return@compute listOf("\n\n", " \n")
            }
        }
    }

    private val STOP_LIST = listOf("}", "]", ")")
    private const val MAX_JUMPING_DISTANCE = 10
}