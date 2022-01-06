package com.github.tomislaw.pickyourautocompletion.settings.component.dialog

import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import javax.swing.JComponent

@Suppress("UnstableApiUsage")
class InstantIntegrationDialog(title: String) : DialogWrapper(true) {

    var apiKey: String = ""

    init {
        this.title = title
        init()
    }

    override fun createCenterPanel(): JComponent = panel {
        row("Api Key") {
            cell(JBPasswordField()).horizontalAlign(HorizontalAlign.FILL).bindText({ apiKey }, { apiKey = it })
        }
    }
}