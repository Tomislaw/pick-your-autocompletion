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
        val element = psi?.findElementAt(offset)

        return@compute when {
            element is PsiComment || element?.prevSibling is PsiComment -> listOf("\n")
            element?.prevSibling is PsiCodeFragment || element?.prevSibling?.prevSibling is PsiCodeFragment ->
                listOf("\n")
            else -> listOf("\n\n", " \n")
        }
    }
}