package com.github.tomislaw.pickyourautocompletion.settings.component.builders

import com.github.tomislaw.pickyourautocompletion.settings.data.AutocompletionData
import com.github.tomislaw.pickyourautocompletion.settings.data.BuiltInRequestBuilderData
import com.github.tomislaw.pickyourautocompletion.settings.data.WebRequestBuilderData
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.layout.ComponentPredicate
import javax.swing.JComponent

class RequestBuilderComponent {

    var type: AutocompletionData.BuilderType
        get() = requestBuilderType.item
        set(value) {
            requestBuilderType.item = value
        }

    var webRequestData: WebRequestBuilderData
        get() = webRequestBuilderComponent.webRequestData
        set(value) {
            webRequestBuilderComponent.webRequestData = value
        }

    var builtInRequestData: BuiltInRequestBuilderData
        get() = builtInRequestBuilderComponent.builtInRequestData
        set(value) {
            builtInRequestBuilderComponent.builtInRequestData = value
        }

    private var requestBuilderType = ComboBox(AutocompletionData.BuilderType.values())
    private fun <T> ComboBox<T>.itemSelected(item: T) = object : ComponentPredicate() {
        override fun invoke(): Boolean = this@itemSelected.selectedItem == item
        override fun addListener(listener: (Boolean) -> Unit) {
            addItemListener { listener(this@itemSelected.selectedItem == item) }
        }
    }

    private val webRequestBuilderComponent = WebRequestBuilderComponent()
    private val builtInRequestBuilderComponent = BuiltInRequestBuilderComponent()

    val panel: DialogPanel = panel {

        group("Request Builder Type") {
            row {
                cell(requestBuilderType).horizontalAlign(HorizontalAlign.FILL)
            }
        }

        group("Built-In Request Builder") {
            builtInRequestBuilderComponent.createComponent(this)
        }.visibleIf(requestBuilderType.itemSelected(AutocompletionData.BuilderType.BuiltIn))
        group("Web Request Builder") {
            webRequestBuilderComponent.createComponent(this)
        }.visibleIf(requestBuilderType.itemSelected(AutocompletionData.BuilderType.Web))
    }

    val preferredFocusedComponent: JComponent
        get() = requestBuilderType

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


}