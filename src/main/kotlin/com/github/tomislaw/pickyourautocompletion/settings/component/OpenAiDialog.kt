package com.github.tomislaw.pickyourautocompletion.settings.component

import com.github.tomislaw.pickyourautocompletion.web.OpenAiApi
import com.intellij.icons.AllIcons
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.ValidationInfo
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.ui.ClickListener
import com.intellij.ui.LayeredIcon
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.*
import com.intellij.ui.dsl.builder.COLUMNS_LARGE
import com.intellij.ui.dsl.builder.columns
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.event.DocumentEvent


@Suppress("UnstableApiUsage")
class OpenAiDialog : DialogWrapper(true) {

    val name = JBTextField()
    val apiKey: JBTextField = JBTextField().apply {
        document.addDocumentListener(object : SimpleDocumentListener {
            override fun update(e: DocumentEvent?) {
                engine.isEnabled = !this@apply.text.isNullOrBlank()
                enginesLabel.isEnabled = !this@apply.text.isNullOrBlank()
            }
        })
    }
    val order = JBTextField().apply { text = "0" }
    val supportedFiles = JBTextField()
    val engine = JBTextField().apply { isEnabled = false }

    private val enginesLabel = JLabel(LayeredIcon(AllIcons.General.GearPlain, AllIcons.General.Dropdown)).apply {
        disabledIcon = AllIcons.General.GearPlain
        isEnabled = false
        object : ClickListener() {
            override fun onClick(e: MouseEvent, clickCount: Int): Boolean {
                if (!this@apply.isEnabled) return true
                showTransformers(this@apply)
                return true
            }
        }.installOn(this)
    }

    init {
        title = "OpenAi Webhook"
        init()
    }

    private fun showTransformers(component: JComponent) {
        OpenAiApi.engineList(apiKey.text)
            .onSuccess {
                JBPopupFactory.getInstance()
                    .createListPopup(object : BaseListPopupStep<String>("Engines", it) {
                        override fun onChosen(selectedValue: String?, finalChoice: Boolean): PopupStep<*>? {
                            engine.text = selectedValue ?: ""
                            return super.onChosen(selectedValue, finalChoice)
                        }
                    })
                    .showUnderneathOf(component)
            }
            .onFailure {
                JBPopupFactory.getInstance()
                    .createMessage("Failed to get transformer list: \n${it.message}")
                    .showUnderneathOf(component)
            }
    }

    override fun createCenterPanel(): JComponent = panel {
        row("Name:") {
            cell(name).horizontalAlign(HorizontalAlign.FILL)
        }
        row("Order:") {
            cell(order).validationOnInput {
                if (order.text.toIntOrNull() == null) ValidationInfo("Not a digit.", order)
                else null
            }.comment(
                "Order for entry point. " +
                        "Available entry point with lowest order will be always used as default one. " +
                        "Prompts from can be displayed using \'Next prompt\'' action or \'Multiple prompts\' action"
            ).horizontalAlign(HorizontalAlign.FILL)
        }
        row("Supported files") {
            cell(supportedFiles).validationOnInput {
                if (supportedFiles.text.isNotEmpty() &&
                    Regex("^\\w+(?:\\,\\w+)*\$").find(supportedFiles.text) == null
                )
                    ValidationInfo(
                        "Invalid format, e.g.: \"py,java,c,kt\".", supportedFiles
                    )
                else
                    null
            }.comment(
                "File types supported by this entry point. " +
                        "Leave empty to accept all file types. " +
                        "E.g.: \"py,java,c,kt\"."
            ).horizontalAlign(HorizontalAlign.FILL)
        }
        row("Api Key:") {
            cell(apiKey).horizontalAlign(HorizontalAlign.FILL)
        }
        row("Engine:") {
            cell(engine).columns(COLUMNS_LARGE).horizontalAlign(HorizontalAlign.FILL)
            cell(enginesLabel).horizontalAlign(HorizontalAlign.RIGHT)
        }
    }
}