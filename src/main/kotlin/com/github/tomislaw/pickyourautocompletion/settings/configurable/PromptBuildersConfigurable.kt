package com.github.tomislaw.pickyourautocompletion.settings.configurable

import com.github.tomislaw.pickyourautocompletion.settings.SettingsStateService
import com.github.tomislaw.pickyourautocompletion.settings.component.builders.PromptBuildersComponent
import com.github.tomislaw.pickyourautocompletion.settings.data.PromptBuilderData
import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import javax.swing.JComponent


class PromptBuildersConfigurable : Configurable {
    private var myComponent: PromptBuildersComponent? = null

    override fun getDisplayName(): String = "Prompt Builder"

    override fun getPreferredFocusedComponent(): JComponent? = myComponent?.preferredFocusedComponent

    override fun createComponent(): JComponent = PromptBuildersComponent().apply {
        instance = this@PromptBuildersConfigurable
        data = service<SettingsStateService>().state.autocompletionData.promptBuilderData
        myComponent = this
    }.panel

    override fun isModified(): Boolean {
        return myComponent?.data != service<SettingsStateService>().state.autocompletionData.promptBuilderData
    }

    override fun apply() {
        service<SettingsStateService>().apply {
            this.state.autocompletionData.promptBuilderData = myComponent?.data ?: PromptBuilderData()
            settingsChanged()
        }
    }

    override fun reset() {
        myComponent?.data = service<SettingsStateService>().state.autocompletionData.promptBuilderData
    }

    override fun disposeUIResources() {
        myComponent = null
        instance = null
    }

    companion object {
        var instance: PromptBuildersConfigurable? = null
            private set
    }
}