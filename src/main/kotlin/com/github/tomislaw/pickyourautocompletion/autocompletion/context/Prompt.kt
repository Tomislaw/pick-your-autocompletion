package com.github.tomislaw.pickyourautocompletion.autocompletion.context

import com.github.tomislaw.pickyourautocompletion.autocompletion.template.VariableTemplateParser
import com.github.tomislaw.pickyourautocompletion.settings.data.PromptBuilderData


class Prompt private constructor(builder: Builder) {
    val text: String
    val language: String
    val directory: String
    val file: String
    val textBefore: String
    val textAfter: String
    val selectedText: String

    init {
        text = builder.prompt
        language = builder.language
        directory = builder.directory
        file = builder.file
        textBefore = builder.textBefore
        textAfter = builder.textAfter
        selectedText = builder.selectedText

    }

    class Builder(private val data: PromptBuilderData) {

        var prompt: String = ""
            private set
        var language: String = ""
            private set
        var directory: String = ""
            private set
        var file: String = ""
            private set
        var textBefore: String = ""
            private set
        var textAfter: String = ""
            private set
        var selectedText: String = ""
            private set
        var promptSize: Int = data.templateSize
            private set

        private val variableParser = VariableTemplateParser()

        val availablePromptSize get() = (data.maxSize - promptSize).coerceAtLeast(0)
        val availableTextBeforeSize get() = data.maxTextBeforeSize.coerceIn(0..availablePromptSize)
        val availableTextAfterSize get() = data.maxTextAfterSize.coerceIn(0..availablePromptSize)

        fun language(language: String) = apply {
            this.language = language
            if (data.containsLanguage) {
                promptSize -= language.length
                variableParser.setVariable(PromptBuilderData.LANGUAGE, language)
            }
        }

        fun directory(directory: String) = apply {
            this.directory = directory
            if (data.containsDirectory) {
                promptSize -= directory.length
                variableParser.setVariable(PromptBuilderData.DIRECTORY, directory)
            }
        }

        fun file(file: String) = apply {
            this.file = file
            if (data.containsFile) {
                promptSize -= file.length
                variableParser.setVariable(PromptBuilderData.FILE, file)
            }
        }

        fun textBefore(textBefore: String) = apply {
            this.textBefore = textBefore
            if (data.containsTextBefore) {
                promptSize -= textBefore.length
                variableParser.setVariable(PromptBuilderData.TEXT_BEFORE, textBefore)
            }
        }

        fun textAfter(textAfter: String) = apply {
            this.textAfter = textAfter
            if (data.containsTextAfter) {
                promptSize -= textAfter.length
                variableParser.setVariable(PromptBuilderData.TEXT_AFTER, textAfter)
            }
        }

        fun selectedText(selectedText: String) = apply {
            this.selectedText = selectedText.takeLast(availablePromptSize)
            promptSize -= selectedText.length
            variableParser.setVariable(PromptBuilderData.SELECTED_TEXT, selectedText)
        }

        fun build(): Prompt {
            prompt = variableParser.parse(data.template).takeLast(data.maxSize)
            return Prompt(this)
        }
    }
}
