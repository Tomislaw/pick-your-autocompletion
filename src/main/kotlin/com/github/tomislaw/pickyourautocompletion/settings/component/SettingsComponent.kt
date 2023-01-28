package com.github.tomislaw.pickyourautocompletion.settings.component

import com.github.tomislaw.pickyourautocompletion.Icons
import com.github.tomislaw.pickyourautocompletion.settings.component.dialog.BuiltInIntegrationDialog
import com.github.tomislaw.pickyourautocompletion.settings.component.dialog.InstantIntegrationDialog
import com.github.tomislaw.pickyourautocompletion.settings.data.PredictionSanitizerData
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.DEFAULT_COMMENT_WIDTH
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.actionListener
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import javax.swing.JButton
import javax.swing.JComponent

class SettingsComponent {

    var liveAutocompletionEnabled: Boolean
        get() = liveAutocompletion.isSelected
        set(value) {
            liveAutocompletion.isSelected = value
        }
    var data: PredictionSanitizerData
        get() = PredictionSanitizerData(
            smartStopTokens.isSelected,
            runCatching { maxPredictionLines.text.toInt() }.getOrDefault(-1).coerceAtLeast(-1),
            if (additionalStopTokens.text.isBlank()) listOf() else additionalStopTokens.text.split(",")
        )
        set(value) {
            smartStopTokens.isSelected = value.smartStopTokens
            maxPredictionLines.text = value.maxLines.toString()
            additionalStopTokens.text = value.additionalStopTokens.joinToString(",")
        }

    private val openAiButton = JButton("OpenAi Integration", Icons.AddOpenAi)
    private val huggingFaceButton = JButton("HuggingFace Integration", Icons.AddHuggingFace)
    private val builtInButton = JButton("Built-in Integration", Icons.AddCli)

    private val liveAutocompletion = JBCheckBox("Live autocompletion")
    private val smartStopTokens: JBCheckBox = JBCheckBox("Smart stop tokens")
    private val maxPredictionLines: JBTextField = JBTextField()
    private val additionalStopTokens: JBTextField = JBTextField()

    val panel: DialogPanel = panel {
        row {
            text(
                "You don't have any defined entry point for this plugin. Without that autocompletion won't work.",
                DEFAULT_COMMENT_WIDTH
            )

        }
        row {
            text(
                "Use one of existing templates to quickly configure this plugin.",
                DEFAULT_COMMENT_WIDTH
            )
        }
        group("Instant Templates") {
            row {
                cell(openAiButton)
                    .horizontalAlign(HorizontalAlign.FILL)
                    .actionListener { _, _ -> InstantIntegrationDialog.addOpenAiIntegration(openAiButton) }
                cell()
            }.layout(RowLayout.PARENT_GRID)
            row {
                cell(huggingFaceButton)
                    .horizontalAlign(HorizontalAlign.FILL)
                    .actionListener { _, _ -> InstantIntegrationDialog.addHuggingFaceIntegration(huggingFaceButton) }
                cell()
            }.layout(RowLayout.PARENT_GRID)
            row {
                cell(builtInButton)
                    .horizontalAlign(HorizontalAlign.FILL)
                    .actionListener { _, _ -> BuiltInIntegrationDialog().showAndGet() }
                cell()
            }.layout(RowLayout.PARENT_GRID)
        }
        group("Autocompletion Settings") {
            row {
                cell(liveAutocompletion)
            }
        }
        group("Autocompletion Sanitization") {
            row {
                cell(smartStopTokens)
            }
            row("Limit prediction text lines") {
                cell(maxPredictionLines)
            }
            row("Additional stop sequences, split by comma") {
                cell(additionalStopTokens).horizontalAlign(HorizontalAlign.FILL)
            }
        }
    }

    val preferredFocusedComponent: JComponent?
        get() = null

}