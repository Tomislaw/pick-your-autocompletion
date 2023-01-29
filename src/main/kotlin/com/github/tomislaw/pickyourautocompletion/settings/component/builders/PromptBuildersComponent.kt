package com.github.tomislaw.pickyourautocompletion.settings.component.builders

import com.github.tomislaw.pickyourautocompletion.localizedText
import com.github.tomislaw.pickyourautocompletion.settings.data.PromptBuilderData
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.UIBundle
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import javax.swing.JComponent

class PromptBuildersComponent {
    var data: PromptBuilderData
        get() = PromptBuilderData(
            template.text,
            runCatching { maxPromptSize.text.toInt() }.getOrDefault(-1).coerceIn(MAX_SIZE_RANGE),
            runCatching { maxTextBeforeSize.text.toInt() }.getOrDefault(-1).coerceIn(MAX_SIZE_RANGE),
            runCatching { maxTextAfterSize.text.toInt() }.getOrDefault(-1).coerceIn(MAX_SIZE_RANGE)
        )
        set(value) {
            template.text = value.template
            maxPromptSize.text = value.maxSize.toString()
            maxTextBeforeSize.text = value.maxTextBeforeSize.toString()
            maxTextAfterSize.text = value.maxTextAfterSize.toString()
        }

    private val maxPromptSize = JBTextField()
    private val maxTextBeforeSize = JBTextField()
    private val maxTextAfterSize = JBTextField()

    private val template = JBTextArea()
    val panel: DialogPanel
        get() = panel {
            row("Maximum prompt size") {
                cell(maxPromptSize).horizontalAlign(HorizontalAlign.FILL)
                    .validationOnInput {
                        when (it.text.toIntOrNull()) {
                            null -> error(UIBundle.message("please.enter.a.number"))
                            !in MAX_SIZE_RANGE -> error(
                                UIBundle.message(
                                    "please.enter.a.number.from.0.to.1",
                                    MAX_SIZE_RANGE.first,
                                    MAX_SIZE_RANGE.last
                                )
                            )

                            else -> null
                        }
                    }
            }
            row("Maximum \${textBefore} size") {
                cell(maxTextBeforeSize).horizontalAlign(HorizontalAlign.FILL)
                    .validationOnInput {
                        when (it.text.toIntOrNull()) {
                            null -> error(UIBundle.message("please.enter.a.number"))
                            !in MAX_SIZE_RANGE -> error(
                                UIBundle.message(
                                    "please.enter.a.number.from.0.to.1",
                                    MAX_SIZE_RANGE.first,
                                    MAX_SIZE_RANGE.last
                                )
                            )

                            else -> null
                        }
                    }
            }
            row("Maximum \${textAfter} size") {
                cell(maxTextAfterSize).horizontalAlign(HorizontalAlign.FILL)
                    .validationOnInput {
                        when (it.text.toIntOrNull()) {
                            null -> error(UIBundle.message("please.enter.a.number"))
                            !in MAX_SIZE_RANGE -> error(
                                UIBundle.message(
                                    "please.enter.a.number.from.0.to.1",
                                    MAX_SIZE_RANGE.first,
                                    MAX_SIZE_RANGE.last
                                )
                            )

                            else -> null
                        }
                    }
            }
            row("Prompt template") {
                cell(template)
                    .horizontalAlign(HorizontalAlign.FILL)
                    .columns(COLUMNS_LARGE)
                    .rows(COLUMNS_TINY * 2)
            }
            row {
                comment(localizedText("settings.promptInfo"))
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

    companion object {
        val MAX_SIZE_RANGE = -1..16384
    }


}