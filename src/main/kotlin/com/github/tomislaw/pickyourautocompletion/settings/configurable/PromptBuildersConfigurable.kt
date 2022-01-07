package com.github.tomislaw.pickyourautocompletion.settings.configurable

import com.github.tomislaw.pickyourautocompletion.autocompletion.PredictiorProviderService
import com.github.tomislaw.pickyourautocompletion.settings.SettingsState
import com.github.tomislaw.pickyourautocompletion.settings.component.PromptBuildersComponent
import com.intellij.openapi.options.Configurable
import javax.swing.JComponent


class PromptBuildersConfigurable : Configurable {
    private var myPromptBuildersComponent: PromptBuildersComponent? = null

    override fun getDisplayName(): String = "Prompt Builder"

    override fun getPreferredFocusedComponent(): JComponent? = myPromptBuildersComponent?.preferredFocusedComponent

    override fun createComponent(): JComponent = PromptBuildersComponent().apply {
        instance = this@PromptBuildersConfigurable
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
        PredictiorProviderService.instance.reload()
    }

    override fun reset() {
        myPromptBuildersComponent?.apply {
            promptBuilders.clear()
            promptBuilders.addAll(SettingsState.instance.promptBuilders)
        }
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