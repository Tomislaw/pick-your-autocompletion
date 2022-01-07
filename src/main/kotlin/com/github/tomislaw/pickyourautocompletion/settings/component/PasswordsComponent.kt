package com.github.tomislaw.pickyourautocompletion.settings.component

import com.github.tomislaw.pickyourautocompletion.settings.component.dialog.MultipleAddEditRemovePanel
import com.github.tomislaw.pickyourautocompletion.settings.data.ApiKey
import com.github.tomislaw.pickyourautocompletion.settings.data.PromptBuilder
import com.intellij.icons.AllIcons
import com.intellij.openapi.options.Configurable
import com.intellij.openapi.options.ConfigurableEP
import com.intellij.openapi.options.ex.ConfigurableExtensionPointUtil
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.layout.panel
import javax.swing.JComponent

class PasswordsComponent {
    private var myApiKeys = mutableListOf<ApiKey>()
    var apiKeys: MutableList<ApiKey>
        get() = myApiKeys
        set(newEntries) {
            myApiKeys = newEntries
            apiKeysTable.data = myApiKeys
        }

    private val apiKeysTable = object : MultipleAddEditRemovePanel<ApiKey>(
        ApiKeysTableModel(),
        myApiKeys,
        setOf(ButtonData("Add", "Add Prompt Builder", AllIcons.General.Add))
    ) {
        override fun addItem(o: ButtonData): ApiKey? {
            return null
        }

        override fun removeItem(o: ApiKey): Boolean {
            return true
        }

        override fun editItem(o: ApiKey): ApiKey? {
            return null
        }
    }.apply { table.setShowColumns(true) }

    val panel: DialogPanel = panel {
        row {
            component(apiKeysTable)
        }
    }

    val preferredFocusedComponent: JComponent
        get() = apiKeysTable

    class ApiKeysTableModel : MultipleAddEditRemovePanel.TableModel<ApiKey>() {

        override fun getField(o: ApiKey, columnIndex: Int): Any = when (columnIndex) {
            0 -> o.name
            1 -> o.id
            else -> ""
        }

        override fun getColumnName(columnIndex: Int): String = when (columnIndex) {
            0 -> "Name"
            1 -> "Id"
            else -> ""
        }

        override val columnCount: Int
            get() = 2
    }
}