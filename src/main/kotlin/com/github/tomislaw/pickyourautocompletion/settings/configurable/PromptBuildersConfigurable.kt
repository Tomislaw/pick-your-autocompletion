package com.github.tomislaw.pickyourautocompletion.settings.configurable

import com.github.tomislaw.pickyourautocompletion.autocompletion.PredictorProviderService
import com.github.tomislaw.pickyourautocompletion.settings.SettingsState
import com.github.tomislaw.pickyourautocompletion.settings.component.PromptBuildersComponent
import com.github.tomislaw.pickyourautocompletion.settings.data.PromptBuilder
import com.intellij.openapi.options.Configurable
import javax.swing.JComponent


class PromptBuildersConfigurable : Configurable {
    private var myPromptBuildersComponent: PromptBuildersComponent? = null

    override fun getDisplayName(): String = "Prompt Builder"

    override fun getPreferredFocusedComponent(): JComponent? = myPromptBuildersComponent?.preferredFocusedComponent

    override fun createComponent(): JComponent = PromptBuildersComponent().apply {
        instance = this@PromptBuildersConfigurable
        data = SettingsState.instance.promptBuilder
        myPromptBuildersComponent = this
    }.panel

    override fun isModified(): Boolean {
        return myPromptBuildersComponent?.data != SettingsState.instance.promptBuilder
    }

    override fun apply() {
        SettingsState.instance.apply {
            this.promptBuilder = myPromptBuildersComponent?.data ?: PromptBuilder()
        }
        PredictorProviderService.instance.reload()
    }

    override fun reset() {
        myPromptBuildersComponent?.data = SettingsState.instance.promptBuilder
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