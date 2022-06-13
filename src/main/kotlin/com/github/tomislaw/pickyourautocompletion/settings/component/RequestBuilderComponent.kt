package com.github.tomislaw.pickyourautocompletion.settings.component

import com.github.tomislaw.pickyourautocompletion.settings.data.RequestBuilder
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.table.JBTable
import javax.swing.JComponent
import javax.swing.table.AbstractTableModel


@Suppress("UnstableApiUsage")
class RequestBuilderComponent {

    var data: RequestBuilder
        get() = RequestBuilder(
            method = method.item,
            url = url.text,
            headers = headers,
            bodyTemplate = body.text,
            bodyParserType = responseParser.item,
            bodyParserData = responseParserData.text,
            timeoutInMillis = timeout.text.toInt(),
            minimumDelayBetweenRequestsInMillis = delay.text.toInt()
        )
        set(value) {
            method.item = value.method
            url.text = value.url
            headers.apply { clear(); addAll(value.headers) }
            body.text = value.bodyTemplate
            responseParser.item = value.bodyParserType
            responseParserData.text = value.bodyParserData
            timeout.text = value.timeoutInMillis.toString()
            delay.text = value.minimumDelayBetweenRequestsInMillis.toString()
        }

    private val headers = mutableListOf<Pair<String, String>>()

    private val url = JBTextField()
    private val method = ComboBox(arrayOf("POST", "GET"))

    private val headersTableModel = HeadersTableModel()
    private val headersTable = JBTable(headersTableModel)

    private val body = JBTextArea()
    private val responseParser = ComboBox(arrayOf("None", "From json", "From Xml", "Regex"))
    private val responseParserData = JBTextField()

    private val timeout = JBTextField()
    private val delay = JBTextField()

    val panel: DialogPanel = panel {
        row("Url:") { cell(url).horizontalAlign(HorizontalAlign.FILL) }
        row("Method:") { cell(method).horizontalAlign(HorizontalAlign.FILL) }
        row("Headers:") { cell(headersTable).horizontalAlign(HorizontalAlign.FILL) }
        row("Body template:") {
            cell(body)
                .horizontalAlign(HorizontalAlign.FILL)
                .columns(COLUMNS_LARGE)
                .rows(COLUMNS_TINY * 2)
        }
        row("Response body parser:") {
            cell(responseParser)
                .horizontalAlign(HorizontalAlign.FILL)
                .columns(COLUMNS_SHORT)
                .applyToComponent {
                    this.addActionListener {
                        responseParserData.selectedParser(this.item)
                    }
                }
            cell(responseParserData)
                .horizontalAlign(HorizontalAlign.FILL)
                .columns(COLUMNS_SHORT)
                .applyToComponent {
                    this.selectedParser(responseParser.item)
                }
        }
        row("Timeout in milliseconds") { cell(timeout).horizontalAlign(HorizontalAlign.FILL) }
        row("Minimum delay between requests in milliseconds") {
            cell(delay).horizontalAlign(HorizontalAlign.FILL)
        }
    }

    val preferredFocusedComponent: JComponent
        get() = url

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
                // do nothing if empty
                if (aValue == null || aValue.toString().isBlank())
                    return

                // add new row
                when (columnIndex) {
                    0 -> headers.add(Pair(aValue.toString(), ""))
                    else -> headers.add(Pair("", aValue.toString()))
                }

            } else {
                when (columnIndex) {
                    0 -> headers[rowIndex] =
                        Pair(aValue.toString(), headers[rowIndex].second)
                    else -> headers[rowIndex] =
                        Pair(headers[rowIndex].first, aValue.toString())
                }
                // if all columns in row are empty then remove header
                if (headers[rowIndex].first.isBlank() && headers[rowIndex].second.isBlank()) {
                    headers.removeAt(rowIndex)
                }
            }
        }
    }

}