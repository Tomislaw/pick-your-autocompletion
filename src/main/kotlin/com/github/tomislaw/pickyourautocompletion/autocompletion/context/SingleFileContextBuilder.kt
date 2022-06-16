package com.github.tomislaw.pickyourautocompletion.autocompletion.context

import com.github.tomislaw.pickyourautocompletion.autocompletion.template.VariableTemplateParser
import com.github.tomislaw.pickyourautocompletion.settings.data.PromptBuilder
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager


class SingleFileContextBuilder(private val prompt: PromptBuilder) : ContextBuilder {

    private val variableParser = VariableTemplateParser()

    override fun create(project: Project, editor: Editor, offset: Int): String =
        ReadAction.compute<String, Throwable> {

            val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)
            val text = editor.document.getText(TextRange(0, offset)).let {
                if (it.length > prompt.maxSize && prompt.maxSize >= 0)
                    it.drop(it.length - prompt.maxSize)
                else
                    it
            }

            if (prompt.template.isBlank())
                return@compute text

            variableParser.setVariable("language", psiFile?.language?.displayName ?: "unknown")
            variableParser.setVariable(
                "directory", psiFile?.containingDirectory?.name
                    ?.replace("\\", "/")
                    ?.removePrefix("Psi") + "/"
            )
            variableParser.setVariable("file", psiFile?.name ?: "")
            variableParser.setVariable("text", text)

            return@compute variableParser.parse(prompt.template)
        }
}
