package com.github.tomislaw.pickyourautocompletion.settings.component

import com.github.tomislaw.pickyourautocompletion.PickYourAutocompletionIcons
import com.github.tomislaw.pickyourautocompletion.settings.component.dialog.MultipleAddEditRemovePanel
import com.github.tomislaw.pickyourautocompletion.settings.component.dialog.WebhookDialog
import com.github.tomislaw.pickyourautocompletion.settings.data.EntryPoint
import com.github.tomislaw.pickyourautocompletion.settings.data.integrations.WebhookIntegration
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.layout.panel
import com.intellij.util.containers.SortedList
import javax.swing.JComponent


class EntryPointsComponent {
    private var myEntryPoints = SortedList<EntryPoint> { o1, o2 -> o1.order.compareTo(o2.order) }
    var entryPoints: SortedList<EntryPoint>
        get() = myEntryPoints
        set(newEntries) {
            myEntryPoints = newEntries
            entryPointTable.data = myEntryPoints
        }

    private val entryPointTable = object : MultipleAddEditRemovePanel<EntryPoint>(
        EntryPointTableModel(),
        myEntryPoints,
        setOf(
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
            EntryPoint.WEBHOOK -> {
                val dialog = WebhookDialog()
                if (dialog.showAndGet()) {
                    dialog.model
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
            EntryPoint.WEBHOOK -> {
                val dialog = WebhookDialog(o as WebhookIntegration)
                if (dialog.showAndGet()) {
                    dialog.model
                } else null
            }
            else -> null
        }
    }.apply { table.setShowColumns(true) }

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
            3 -> o.supportedFiles.let { it ->
                if (it.isEmpty()) "all files" else
                    it.joinToString(",") { "*.$it" }
            }
            4 -> o.promptBuilder
            else -> ""
        }

        override fun getColumnName(columnIndex: Int): String = when (columnIndex) {
            0 -> "Name"
            1 -> "Order"
            2 -> "Type"
            3 -> "Supported Files"
            4 -> "Prompt Builder"
            else -> ""
        }

        override val columnCount: Int
            get() = 5

    }
}