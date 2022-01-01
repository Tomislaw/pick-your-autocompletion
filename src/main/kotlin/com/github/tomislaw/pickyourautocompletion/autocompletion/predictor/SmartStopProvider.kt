package com.github.tomislaw.pickyourautocompletion.autocompletion.predictor

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.psi.PsiManager

object SmartStopProvider {

    fun getStopString(offset: Int, editor: Editor): String {

        if (editor !is EditorImpl || offset > editor.document.textLength)
            return "\n"
        val project = editor.project ?: return "\n"

        val psi = PsiManager.getInstance(project).findFile(editor.virtualFile)
        psi?.findElementAt(offset)
        return "\n\n"
    }
}