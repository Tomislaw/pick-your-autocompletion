package com.github.tomislaw.pickyourautocompletion.settings.component

import com.github.tomislaw.pickyourautocompletion.settings.data.PromptBuilder
import com.intellij.openapi.ui.DialogPanel
import com.intellij.ui.layout.panel
import javax.swing.JComponent

class PromptBuildersComponent {
    var data: PromptBuilder
        get() = PromptBuilder(
            )
        set(value) {}

    val panel: DialogPanel = panel {
        row {
            //component(promptBuildersTable)
        }
    }

    val preferredFocusedComponent: JComponent
        get() = panel

}