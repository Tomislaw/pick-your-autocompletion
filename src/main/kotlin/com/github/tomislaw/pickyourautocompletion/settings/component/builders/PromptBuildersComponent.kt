package com.github.tomislaw.pickyourautocompletion.settings.component.builders

import com.github.tomislaw.pickyourautocompletion.settings.data.PromptBuilderData
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import javax.swing.JComponent

class PromptBuildersComponent {
    var data: PromptBuilderData
        get() = PromptBuilderData(
            template.text,
            runCatching { maxPromptSize.text.toInt() }.getOrDefault(-1)
        )
        set(value) {
            template.text = value.template
            maxPromptSize.text = value.maxSize.toString()
        }

    private val maxPromptSize = JBTextField()
    private val template = JBTextArea()
    val panel: DialogPanel get() =  panel {
        row("Maximum prompt size") {
            cell(maxPromptSize).horizontalAlign(HorizontalAlign.FILL)
        }
        row("Prompt template") {
            cell(template)
                .horizontalAlign(HorizontalAlign.FILL)
                .columns(COLUMNS_LARGE)
                .rows(COLUMNS_TINY * 2)
        }
    }

    val preferredFocusedComponent: JComponent
        get() = panel

    private fun JBTextField.selectedParser(prompt: String) {
        when (prompt) {
            "From Xml" -> {
                this.isEnabled = true
                this.emptyText.text = "e.g. data/0/value"
            }

            "From json" -> {
                this.isEnabled = true
                this.emptyText.text = "e.g. data/0/value"
            }

            "Regex" -> {
                this.isEnabled = true
                this.emptyText.text = "todo"
            }

            else -> {
                this.isEnabled = false
                this.emptyText.text = ""
            }
        }
    }


}