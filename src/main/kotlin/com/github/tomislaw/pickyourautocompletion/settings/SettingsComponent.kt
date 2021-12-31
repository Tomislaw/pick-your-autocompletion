package com.github.tomislaw.pickyourautocompletion.settings

import com.github.tomislaw.pickyourautocompletion.settings.component.MultipleAddEditRemovePanel
import com.github.tomislaw.pickyourautocompletion.settings.component.OpenAiDialog
import com.github.tomislaw.pickyourautocompletion.settings.data.EntryPoint
import com.github.tomislaw.pickyourautocompletion.settings.data.webhook.WebhookData
import com.intellij.icons.AllIcons
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.components.JBCheckBox
import com.intellij.ui.components.JBTextField
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
            ButtonData(EntryPoint.OPENAI, "Add OpenAi Webhook", AllIcons.General.Add),
            ButtonData(EntryPoint.WEBHOOK, "Add Custom Webhook", AllIcons.General.Add),
            ButtonData(EntryPoint.SCRIPT, "Add CLI Entry Point", AllIcons.General.Add),
        )
    ) {
        override fun addItem(o: ButtonData): EntryPoint? = when (o.text) {
            EntryPoint.OPENAI -> {
                val dialog = OpenAiDialog()
                if (dialog.showAndGet()) {
                    WebhookData.openAi(
                        name = dialog.name.text.uniqueName(),
                        apiKey = dialog.apiKey.text,
                        engine = dialog.transformer.text
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
        }

        override fun removeItem(o: EntryPoint): Boolean {
            myEntryPoints.remove(o)
            return true
        }

        override fun editItem(o: EntryPoint): EntryPoint? = when (o.type) {
            EntryPoint.OPENAI -> {
                val dialog = OpenAiDialog()
                if (dialog.showAndGet()) {
                    WebhookData.openAi(
                        name = o.name.uniqueName(o),
                        apiKey = (o as WebhookData).request.headers
                            .getOrDefault("Authorization", "")
                            .removePrefix("Bearer"),
                        engine = "todo"
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

    private val myUserNameText = JBTextField()
    private val myIdeaUserStatus = JBCheckBox("Do you use IntelliJ IDEA? ")


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