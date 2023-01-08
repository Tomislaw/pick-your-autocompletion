package com.github.tomislaw.pickyourautocompletion.settings.configurable

import com.github.tomislaw.pickyourautocompletion.settings.SettingsStateService
import com.github.tomislaw.pickyourautocompletion.settings.component.builders.PromptBuildersComponent
import com.github.tomislaw.pickyourautocompletion.settings.data.PromptBuilderData
import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import javax.swing.JComponent


class PromptBuildersConfigurable : Configurable {
    private var myPromptBuildersComponent: PromptBuildersComponent? = null

    override fun getDisplayName(): String = "Prompt Builder"

    override fun getPreferredFocusedComponent(): JComponent? = myPromptBuildersComponent?.preferredFocusedComponent

    override fun createComponent(): JComponent = PromptBuildersComponent().apply {
        instance = this@PromptBuildersConfigurable
        data = service<SettingsStateService>().state.autocompletionData.promptBuilderData
        myPromptBuildersComponent = this
    }.panel

    override fun isModified(): Boolean {
        return myPromptBuildersComponent?.data != service<SettingsStateService>().state.autocompletionData.promptBuilderData
    }

    override fun apply() {
        service<SettingsStateService>().apply {
            this.state.autocompletionData.promptBuilderData = myPromptBuildersComponent?.data ?: PromptBuilderData()
            settingsChanged()
        }
    }

    override fun reset() {
        myPromptBuildersComponent?.data = service<SettingsStateService>().state.autocompletionData.promptBuilderData
    }

    override fun disposeUIResources() {
        myPromptBuildersComponent = null
        instance = null
    }

    companion object {
        var instance: PromptBuildersConfigurable? = null
            private set
    }
}