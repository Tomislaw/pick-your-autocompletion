package com.github.tomislaw.pickyourautocompletion.autocompletion.context

import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager


class MultiFileContextBuilder : ContextBuilder {

    override fun create(project: Project, editor: Editor, offset: Int): String =
        ReadAction.compute<String, Throwable> {

            val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)

            val text = editor.document.getText(TextRange(0, offset))
            return@compute StringBuilder()
                .appendLine("Language: " + psiFile?.language?.displayName)
                .appendLine(
                    "Directory: " + psiFile?.containingDirectory?.name?.replace("\\", "/")
                        ?.removePrefix("Psi") + "/" + psiFile?.name
                )
                .appendLine("###")
                .append(text)
                .toString()
        }
}
