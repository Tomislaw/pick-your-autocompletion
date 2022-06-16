package com.github.tomislaw.pickyourautocompletion.settings.component

import com.github.tomislaw.pickyourautocompletion.settings.data.PromptBuilder
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import javax.swing.JComponent

@Suppress("UnstableApiUsage")
class PromptBuildersComponent {
    var data: PromptBuilder
        get() = PromptBuilder(
            template.text,
            runCatching { maxPromptSize.text.toInt() }.getOrDefault(-1)
        )
        set(value) {
            template.text = value.template
            maxPromptSize.text = value.maxSize.toString()
        }

    private val maxPromptSize = JBTextField()
    private val template = JBTextArea()
    val panel: DialogPanel = panel {
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

}