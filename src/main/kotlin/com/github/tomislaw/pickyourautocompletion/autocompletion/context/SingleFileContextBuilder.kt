package com.github.tomislaw.pickyourautocompletion.autocompletion.context

import com.github.tomislaw.pickyourautocompletion.settings.data.PromptBuilderData
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager


class SingleFileContextBuilder(private val promptData: PromptBuilderData) : ContextBuilder {

    override fun create(project: Project, editor: Editor, offset: Int): Prompt =
        ReadAction.compute<Prompt, Throwable> {

            val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
            val language = psiFile?.language?.displayName ?: ""
            val file = psiFile?.name ?: ""
            val directory = psiFile?.containingDirectory?.name
                ?.replace("\\", "/")
                ?.removePrefix("Psi") + "/"

            return@compute Prompt.Builder(promptData)
                .language(language)
                .file(file)
                .directory(directory)
                .apply {
                    val bytesLeft = availableTextBeforeSize
                    val startingOffset = (offset - bytesLeft).coerceAtLeast(0)
                    textBefore(editor.document.getText(TextRange(startingOffset, offset)))
                }
                .apply {
                    val bytesLeft = availableTextAfterSize
                    val endingOffset = (offset + bytesLeft).coerceAtMost(editor.document.textLength)
                    textAfter(editor.document.getText(TextRange(offset, endingOffset)))
                }
                .build()
        }
}
