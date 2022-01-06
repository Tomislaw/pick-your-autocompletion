package com.github.tomislaw.pickyourautocompletion.settings.component

import com.github.tomislaw.pickyourautocompletion.settings.component.dialog.MultipleAddEditRemovePanel
import com.github.tomislaw.pickyourautocompletion.settings.data.PromptBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.layout.panel
import javax.swing.JComponent

class PromptBuildersComponent {
    private var myPromptBuilders = mutableListOf<PromptBuilder>()
    var promptBuilders: MutableList<PromptBuilder>
        get() = myPromptBuilders
        set(newEntries) {
            myPromptBuilders = newEntries
            promptBuildersTable.data = myPromptBuilders
        }

    private val promptBuildersTable = object : MultipleAddEditRemovePanel<PromptBuilder>(
        PromptBuilderTableModel(),
        myPromptBuilders,
        setOf(ButtonData("Add", "Add Prompt Builder", AllIcons.General.Add))
    ) {
        override fun addItem(o: ButtonData): PromptBuilder? {
            return null
        }

        override fun removeItem(o: PromptBuilder): Boolean {
            return true
        }

        override fun editItem(o: PromptBuilder): PromptBuilder? {
            return null
        }
    }.apply { table.setShowColumns(true) }

    val panel: DialogPanel = panel {
        row {
            component(promptBuildersTable)
        }
    }

    val preferredFocusedComponent: JComponent
        get() = promptBuildersTable

    class PromptBuilderTableModel : MultipleAddEditRemovePanel.TableModel<PromptBuilder>() {

        override fun getField(o: PromptBuilder, columnIndex: Int): Any = when (columnIndex) {
            0 -> o.name
            1 -> o.maxSize
            else -> ""
        }

        override fun getColumnName(columnIndex: Int): String = when (columnIndex) {
            0 -> "Name"
            1 -> "Max Size"
            else -> ""
        }

        override val columnCount: Int
            get() = 5
    }
}