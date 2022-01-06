package com.github.tomislaw.pickyourautocompletion.settings.component

import com.github.tomislaw.pickyourautocompletion.PickYourAutocompletionIcons
import com.github.tomislaw.pickyourautocompletion.settings.SettingsState
import com.github.tomislaw.pickyourautocompletion.settings.component.dialog.InstantIntegrationDialog
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import javax.swing.JButton
import javax.swing.JComponent

@Suppress("UnstableApiUsage")
class SettingsComponent {

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
                cell(JButton("Instant OpenAi Integration", PickYourAutocompletionIcons.AddOpenAi))
                    .horizontalAlign(HorizontalAlign.FILL)
                    .actionListener { _, _ ->
                        InstantIntegrationDialog("OpenAi Integration").apply {
                            showAndGet()
//                            val apiKey = ApiKey.create(
//                                "OpenAi Api Key".withUniqueName(SettingsState.instance.passwords),
//                                this.apiKey
//                            )
//                            SettingsState.instance.passwords.add(apiKey)
                        }
                    }
                cell()
            }.layout(RowLayout.PARENT_GRID)
            row {
                cell(JButton("Instant HuggingFace Integration", PickYourAutocompletionIcons.AddHuggingFace))
                    .horizontalAlign(HorizontalAlign.FILL)
                    .actionListener { _, _ ->
                        InstantIntegrationDialog("HuggingFace Integration").apply {
                            showAndGet()

                        }
                    }
                cell()
            }.layout(RowLayout.PARENT_GRID)
        }
    }

    val preferredFocusedComponent: JComponent?
        get() = null

}