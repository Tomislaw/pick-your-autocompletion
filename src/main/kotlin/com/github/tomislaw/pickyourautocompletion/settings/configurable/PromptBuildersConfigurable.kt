package com.github.tomislaw.pickyourautocompletion.settings.configurable

import com.github.tomislaw.pickyourautocompletion.settings.SettingsState
import com.github.tomislaw.pickyourautocompletion.settings.component.PromptBuildersComponent
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.options.Configurable
import javax.swing.JComponent


class PromptBuildersConfigurable : Configurable {
    private var myPromptBuildersComponent: PromptBuildersComponent? = null

    override fun getDisplayName(): String = "Prompt Builder"

    override fun getPreferredFocusedComponent(): JComponent? = myPromptBuildersComponent?.preferredFocusedComponent

    override fun createComponent(): JComponent = PromptBuildersComponent().apply {
        promptBuilders.addAll(SettingsState.instance.promptBuilders)
        myPromptBuildersComponent = this
    }.panel

    override fun isModified(): Boolean {
        return myPromptBuildersComponent?.promptBuilders != SettingsState.instance.promptBuilders
    }

    override fun apply() {
        SettingsState.instance.apply {
            this.promptBuilders.clear()
            this.promptBuilders.addAll(myPromptBuildersComponent?.promptBuilders ?: emptyList())
        }
    }

    override fun reset() {
        myPromptBuildersComponent?.apply {
            promptBuilders.clear()
            promptBuilders.addAll(SettingsState.instance.promptBuilders)
        }
    }

    override fun disposeUIResources() {
        myPromptBuildersComponent = null
    }

    companion object {
        val instance: PromptBuildersConfigurable
            get() = ApplicationManager.getApplication().getService(PromptBuildersConfigurable::class.java)
    }
}