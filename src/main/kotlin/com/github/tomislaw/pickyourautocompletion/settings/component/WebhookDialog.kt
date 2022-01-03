package com.github.tomislaw.pickyourautocompletion.settings.component


import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.table.JBTable
import javax.swing.JComponent
import javax.swing.table.AbstractTableModel

@Suppress("UnstableApiUsage")
class WebhookDialog : DialogWrapper(true) {

    val name = JBTextField()
    val order = JBTextField().apply { text = "0" }
    val supportedFiles = JBTextField()
    val headers = mutableListOf<Pair<String, String>>()
    private val model = HeadersTableModel()
    private val headersTable = JBTable(model).apply {
        //visibleRowCount = 1
    }
    // val name = JBTextField()

    init {
        title = "Webhook"
        init()
    }

    override fun createCenterPanel(): JComponent = panel {
        row("Name:") {
            cell(name).horizontalAlign(HorizontalAlign.FILL)
        }
        row("Order:") {
            cell(order).validationOnInput {
                if (order.text.toIntOrNull() == null) ValidationInfo("Not a digit.", order)
                else null
            }.horizontalAlign(HorizontalAlign.FILL)
        }
        row("Supported files") {
            cell(supportedFiles).validationOnInput {
                if (supportedFiles.text.isNotEmpty() &&
                    Regex("^\\w+(?:\\,\\w+)*\$").find(supportedFiles.text) == null
                )
                    ValidationInfo(
                        "Invalid format, e.g.: \"py,java,c,kt\". " +
                                "Leave empty if you want to accept all file types.", supportedFiles
                    )
                else
                    null
            }.horizontalAlign(HorizontalAlign.FILL)
        }
        row("Headers") {
            cell(headersTable).horizontalAlign(HorizontalAlign.FILL)
        }
//        row("Api Key:") {
//            component(apiKey)
//        }
//        row("Engine:") {
//            component(engine)
//            component(enginesLabel)
//        }


    }

    inner class HeadersTableModel : AbstractTableModel() {
        override fun getRowCount(): Int = headers.size + 1
        override fun getColumnCount(): Int = 2
        override fun getColumnName(columnIndex: Int): String = when (columnIndex) {
            0 -> "Name"
            else -> "Value"
        }

        override fun getColumnClass(columnIndex: Int): Class<*> = String::class.java
        override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = true
        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any = when {
            rowIndex >= headers.size -> ""
            columnIndex == 0 -> headers[rowIndex].first
            else -> headers[rowIndex].second
        }

        override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
            if (rowIndex >= headers.size) {
                when (columnIndex) {
                    0 -> headers.add(Pair(aValue.toString(), ""))
                    else -> headers.add(Pair("", aValue.toString()))
                }
                this@WebhookDialog.validate()
            } else {
                when (columnIndex) {
                    0 -> headers[rowIndex] = Pair(aValue.toString(), headers[rowIndex].second)
                    else -> headers[rowIndex] = Pair(headers[rowIndex].first, aValue.toString())
                }
                if (headers[rowIndex].first.isBlank() && headers[rowIndex].second.isBlank()) {
                    headers.removeAt(rowIndex)
                    this@WebhookDialog.validate()
                }
            }


        }

    }
}