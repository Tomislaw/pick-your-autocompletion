package com.github.tomislaw.pickyourautocompletion.settings

import com.github.tomislaw.pickyourautocompletion.PickYourAutocompletionIcons
import com.github.tomislaw.pickyourautocompletion.settings.component.MultipleAddEditRemovePanel
import com.github.tomislaw.pickyourautocompletion.settings.component.OpenAiDialog
import com.github.tomislaw.pickyourautocompletion.settings.component.WebhookDialog
import com.github.tomislaw.pickyourautocompletion.settings.data.EntryPoint
import com.github.tomislaw.pickyourautocompletion.settings.data.integrations.OpenAiIntegration
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.layout.panel
import com.intellij.util.containers.SortedList
import javax.swing.JComponent


class SettingsComponent {
    private var myEntryPoints = SortedList<EntryPoint> { o1, o2 -> o1.order.compareTo(o2.order) }
    var entryPoints: SortedList<EntryPoint>
        get() = myEntryPoints
        set(newEntries) {
            myEntryPoints = newEntries
            entryPointTable.data = myEntryPoints
        }

    private fun String.uniqueName(entryPoint: EntryPoint? = null): String {
        val name = this.ifBlank { "New entry point" }
        var nextName = name
        while (entryPoints.find { it.name == nextName && it !== entryPoint } != null)
            nextName += " new"
        return nextName
    }

    private val entryPointTable = object : MultipleAddEditRemovePanel<EntryPoint>(
        EntryPointTableModel(),
        myEntryPoints,
        setOf(
            ButtonData(
                EntryPoint.OPENAI, "Add OpenAi Webhook",
                PickYourAutocompletionIcons.AddOpenAi
            ),
//            ButtonData(
//                EntryPoint.HUGGINGFACE, "Add Hugging Face Webhook",
//                PickYourAutocompletionIcons.AddHuggingFace
//            ),
            ButtonData(
                EntryPoint.WEBHOOK, "Add Custom Webhook",
                PickYourAutocompletionIcons.AddWebhook
            ),
            ButtonData(
                EntryPoint.SCRIPT, "Add CLI Entry Point",
                PickYourAutocompletionIcons.AddCli
            ),
        )
    ) {
        override fun addItem(o: ButtonData): EntryPoint? = when (o.text) {
            EntryPoint.OPENAI -> {
                val dialog = OpenAiDialog()
                if (dialog.showAndGet()) {
                    OpenAiIntegration(
                        name =  dialog.name.text.uniqueName(),
                        apiKey = dialog.apiKey.text,
                        engine = dialog.engine.text
                    )
                } else null
            }
            EntryPoint.WEBHOOK -> {
                val dialog = WebhookDialog()
                if (dialog.showAndGet()) {
                    null
                } else null
            }
            else -> {
                null
            }
        }

        override fun removeItem(o: EntryPoint): Boolean {
            return true
        }

        override fun editItem(o: EntryPoint): EntryPoint? = when (o.type) {
            EntryPoint.OPENAI -> {
                val openai = o as OpenAiIntegration
                val dialog = OpenAiDialog().apply {
                    this.name.text = openai.name
                    this.apiKey.text = openai.apiKey
                    this.engine.text = openai.engine
                }
                if (dialog.showAndGet()) {
                    OpenAiIntegration(
                        name =  dialog.name.text.uniqueName(o),
                        apiKey = dialog.apiKey.text,
                        engine = dialog.engine.text
                    )
                } else null
            }
            EntryPoint.WEBHOOK -> {
                ApplicationManager.getApplication().invokeLater {
                    JBPopupFactory.getInstance().createMessage(
                        "Failed to get list of transformer"
                    ).showUnderneathOf(panel)

                }
                null
            }
            else -> {
                null
            }
        }.apply {
            myEntryPoints.add(this)
        }

    }

    val panel: DialogPanel = panel {
        row {
            component(entryPointTable)
        }
    }


    val preferredFocusedComponent: JComponent
        get() = entryPointTable


    class EntryPointTableModel : MultipleAddEditRemovePanel.TableModel<EntryPoint>() {

        override fun getField(o: EntryPoint, columnIndex: Int): Any = when (columnIndex) {
            0 -> o.name
            1 -> o.order
            2 -> o.type
            3 -> o.supportedFiles
            4 -> o.promptBuilder
            else -> {}
        }

        override fun getColumnName(columnIndex: Int): String = when (columnIndex) {
            0 -> "Name"
            1 -> "Order"
            2 -> "Type"
            3 -> "Supported Files"
            4 -> "PromptBuilder"
            else -> ""
        }

        override val columnCount: Int
            get() = 5

    }
}