package com.github.tomislaw.pickyourautocompletion.settings.component

import com.github.tomislaw.pickyourautocompletion.web.OpenAiApi
import com.intellij.icons.AllIcons
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.ui.popup.PopupStep
import com.intellij.openapi.ui.popup.util.BaseListPopupStep
import com.intellij.ui.ClickListener
import com.intellij.ui.LayeredIcon
import com.intellij.ui.components.JBTextField
import com.intellij.ui.layout.panel
import java.awt.event.MouseEvent
import javax.swing.JComponent
import javax.swing.JLabel
import javax.swing.event.DocumentEvent


class OpenAiDialog : DialogWrapper(true) {

    val name = JBTextField()
    val apiKey: JBTextField = JBTextField().apply {
        document.addDocumentListener(object : SimpleDocumentListener {
            override fun update(e: DocumentEvent?) {
                transformer.isEnabled = !this@apply.text.isNullOrBlank()
                transformersLabel.isEnabled = !this@apply.text.isNullOrBlank()
            }
        })
    }

    val transformer = JBTextField().apply { isEnabled = false }

    private val transformersLabel = JLabel(LayeredIcon(AllIcons.General.GearPlain, AllIcons.General.Dropdown)).apply {
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
                            transformer.text = selectedValue ?: ""
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
            component(name)
        }
        row("Api Key:") {
            component(apiKey)
        }
        row("Transformer:") {
            component(transformer)
            component(transformersLabel)
        }
    }
}