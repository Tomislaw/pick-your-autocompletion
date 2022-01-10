package com.github.tomislaw.pickyourautocompletion.settings.component.dialog


import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.webhook.parser.JsonBodyParser
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.webhook.parser.RegexBodyParser
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.webhook.parser.XmlBodyParser
import com.github.tomislaw.pickyourautocompletion.settings.data.integrations.WebhookIntegration
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.table.JBTable
import javax.swing.JComponent
import javax.swing.table.AbstractTableModel

@Suppress("UnstableApiUsage")
class WebhookDialog(
    val model: WebhookIntegration = WebhookIntegration()
) : DialogWrapper(true) {
    private val headersTableModel = HeadersTableModel()
    private val headersTable = JBTable(headersTableModel)

    private var parser: String = ""
    private var parserData: String = ""

    init {
        title = "Webhook"
        when (model.request.bodyParser) {
            is JsonBodyParser -> {
                parser = "From json"
                parserData = (model.request.bodyParser as JsonBodyParser).path
            }
            is XmlBodyParser -> {
                parser = "From Xml"
                parserData = (model.request.bodyParser as XmlBodyParser).path
            }
            is RegexBodyParser -> {
                parser = "Regex"
                parserData = (model.request.bodyParser as RegexBodyParser).regex
            }
            else -> {
                parser = "None"
                parserData = ""
            }
        }

        init()
    }


    override fun doOKAction() {
        super.doOKAction()
        model.request.bodyParser = when (parser) {
            "From Xml" -> XmlBodyParser(parserData)
            "From json" -> JsonBodyParser(parserData)
            "Regex" -> RegexBodyParser(parserData)
            else -> null
        }
    }

    override fun createCenterPanel(): JComponent = panel {
        row("Name:") {
            textField()
                .bindText(model::name)
                .horizontalAlign(HorizontalAlign.FILL)
                .columns(COLUMNS_LARGE)
        }
        row("Order:") {
            textField()
                .bindIntText(model::order)
                .horizontalAlign(HorizontalAlign.FILL)
                .columns(COLUMNS_LARGE)
        }
        row("Supported files:") {
            textField()
                .bindText(
                    { model.supportedFiles.joinToString(",") },
                    { model.supportedFiles = it.split(",") }
                )
                .horizontalAlign(HorizontalAlign.FILL)
                .columns(COLUMNS_LARGE)
                .validationOnInput {
                    val input = (this.component as JBTextField).text
                    if (input.isNotEmpty() && Regex("^\\w+(?:\\,\\w+)*\$").find(input) == null) {
                        ValidationInfo(
                            "Invalid format, e.g.: \"py,java,c,kt\". " +
                                    "Leave empty if you want to accept all file types.", component
                        )
                    } else null
                }
        }
        row("Url:") {
            textField()
                .bindText(model.request::url)
                .horizontalAlign(HorizontalAlign.FILL)
                .columns(COLUMNS_LARGE)
        }
        row("Method:") {
            comboBox(arrayOf("POST", "GET"))
                .bindItem(model.request::method)
                .horizontalAlign(HorizontalAlign.FILL)
                .columns(COLUMNS_SHORT)
        }
        row("Headers:") {
            cell(headersTable)
                .horizontalAlign(HorizontalAlign.FILL)
        }
        row("Body template:") {
            textArea()
                .bindText(model.request::bodyTemplate)
                .horizontalAlign(HorizontalAlign.FILL)
                .columns(COLUMNS_LARGE)
                .rows(COLUMNS_TINY * 2)
        }
        row("Max body size:") { // todo temove it
            textArea()
                .bindIntText(model.request::maxSize)
                .horizontalAlign(HorizontalAlign.FILL)
                .columns(COLUMNS_SHORT)
        }
        row("Response body parser:") {
            lateinit var parserDataCell: Cell<JBTextField>
            val parser = comboBox(arrayOf("None", "From json", "From Xml", "Regex"))
                .bindItem(
                    { parser },
                    { parser = it ?: "" }
                )
                .horizontalAlign(HorizontalAlign.FILL)
                .columns(COLUMNS_SHORT)
                .applyToComponent {
                    this.addActionListener {
                        parserDataCell.component.selectedParser(this.item)
                    }
                }
            parserDataCell = textField()
                .horizontalAlign(HorizontalAlign.FILL)
                .bindText(
                    { parserData },
                    { parserData = it }
                )
                .columns(COLUMNS_SHORT)
                .applyToComponent {
                    selectedParser(parser.component.item)
                }
        }
        row("Timeout in seconds") {
            textField()
                .bindIntText(model.request::timeout)
                .horizontalAlign(HorizontalAlign.FILL)
                .columns(COLUMNS_LARGE)
        }
    }

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
        override fun getRowCount(): Int = model.request.headers.size + 1
        override fun getColumnCount(): Int = 2
        override fun getColumnName(columnIndex: Int): String = when (columnIndex) {
            0 -> "Name"
            else -> "Value"
        }

        override fun getColumnClass(columnIndex: Int): Class<*> = String::class.java
        override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = true
        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any = when {
            rowIndex >= model.request.headers.size -> ""
            columnIndex == 0 -> model.request.headers[rowIndex].first
            else -> model.request.headers[rowIndex].second
        }

        override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
            if (rowIndex >= model.request.headers.size) {
                when (columnIndex) {
                    0 -> model.request.headers.add(Pair(aValue.toString(), ""))
                    else -> model.request.headers.add(Pair("", aValue.toString()))
                }
                this@WebhookDialog.validate()
            } else {
                when (columnIndex) {
                    0 -> model.request.headers[rowIndex] =
                        Pair(aValue.toString(), model.request.headers[rowIndex].second)
                    else -> model.request.headers[rowIndex] =
                        Pair(model.request.headers[rowIndex].first, aValue.toString())
                }
                if (model.request.headers[rowIndex].first.isBlank() && model.request.headers[rowIndex].second.isBlank()) {
                    model.request.headers.removeAt(rowIndex)
                    this@WebhookDialog.validate()
                }
            }
        }
    }
}