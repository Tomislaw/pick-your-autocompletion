package com.github.tomislaw.pickyourautocompletion.autocompletion.context

import com.github.tomislaw.pickyourautocompletion.autocompletion.template.VariableTemplateParser
import com.github.tomislaw.pickyourautocompletion.settings.data.PromptBuilderData
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager


class SingleFileContextBuilder(private val prompt: PromptBuilderData) : ContextBuilder {

    private val variableParser = VariableTemplateParser()

    override fun create(project: Project, editor: Editor, offset: Int): String =
        ReadAction.compute<String, Throwable> {

            if (prompt.maxSize < prompt.templateSize || prompt.maxSize <= 0)
                return@compute variableParser.parse(prompt.template)
                    .takeLast(prompt.maxSize.coerceAtLeast(0))

            var totalSize = prompt.templateSize
            val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)

            if (prompt.containsLanguage) {
                val language = psiFile?.language?.displayName ?: ""
                variableParser.setVariable(PromptBuilderData.LANGUAGE, language)
                totalSize += language.length
                if (totalSize >= prompt.maxSize)
                    return@compute variableParser.parse(prompt.template).takeLast(prompt.maxSize)
            }

            if (prompt.containsFile) {
                val file = psiFile?.name ?: ""
                totalSize += file.length
                variableParser.setVariable(PromptBuilderData.FILE, file)
                if (totalSize >= prompt.maxSize)
                    return@compute variableParser.parse(prompt.template).takeLast(prompt.maxSize)
            }

            if (prompt.containsDirectory) {
                val directory = psiFile?.containingDirectory?.name
                    ?.replace("\\", "/")
                    ?.removePrefix("Psi") + "/"
                variableParser.setVariable(PromptBuilderData.DIRECTORY, directory)
                totalSize += directory.length
                if (totalSize >= prompt.maxSize)
                    return@compute variableParser.parse(prompt.template).takeLast(prompt.maxSize)
            }

            if (prompt.containsTextBefore) {
                val bytesLeft = (prompt.maxSize - totalSize).coerceAtMost(prompt.maxTextBeforeSize)

                val startingOffset = if (offset > bytesLeft) offset - bytesLeft else 0
                val textBefore = editor.document.getText(TextRange(startingOffset, offset))
                variableParser.setVariable(PromptBuilderData.TEXT_BEFORE, textBefore)
                totalSize += textBefore.length
                if (totalSize >= prompt.maxSize)
                    return@compute variableParser.parse(prompt.template).takeLast(prompt.maxSize)
            }

            if (prompt.containsTextAfter) {
                val bytesLeft = (prompt.maxSize - totalSize).coerceAtMost(prompt.maxTextAfterSize)
                val endingOffset = if (editor.document.textLength - offset > bytesLeft)
                    offset + bytesLeft
                else
                    editor.document.textLength
                val textAfter = editor.document.getText(TextRange(offset, endingOffset))
                variableParser.setVariable(PromptBuilderData.TEXT_AFTER, textAfter)
                totalSize += textAfter.length
                if (totalSize >= prompt.maxSize)
                    return@compute variableParser.parse(prompt.template).takeLast(prompt.maxSize)
            }

            return@compute variableParser.parse(prompt.template).takeLast(prompt.maxSize)
        }
}
