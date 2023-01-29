package com.github.tomislaw.pickyourautocompletion.settings.configurable

import com.github.tomislaw.pickyourautocompletion.settings.SettingsStateService
import com.github.tomislaw.pickyourautocompletion.settings.component.SettingsComponent
import com.github.tomislaw.pickyourautocompletion.settings.data.PredictionSanitizerData
import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

/**
 * Provides controller functionality for application settings.
 */
class SettingsConfigurable : Configurable {
    private var myComponent: SettingsComponent? = null

    override fun getDisplayName(): String = "Pick Your Autocompletion"

    override fun getPreferredFocusedComponent(): JComponent? = myComponent?.preferredFocusedComponent

    override fun createComponent(): JComponent = SettingsComponent().apply {
        val state = service<SettingsStateService>().state
        instance = this@SettingsConfigurable
        data = state.autocompletionData.predictionSanitizerData
        liveAutocompletionEnabled = state.liveAutoCompletionEnabled
        myComponent = this
    }.panel

    override fun isModified(): Boolean {
        val state = service<SettingsStateService>().state
        return myComponent?.data != state.autocompletionData.predictionSanitizerData
                || myComponent?.liveAutocompletionEnabled != state.liveAutoCompletionEnabled
    }

    override fun apply() {
        service<SettingsStateService>().apply {
            state.autocompletionData.predictionSanitizerData = myComponent?.data ?: PredictionSanitizerData()
            state.liveAutoCompletionEnabled = myComponent?.liveAutocompletionEnabled ?: false
            settingsChanged()
        }
    }

    override fun reset() {
        val state = service<SettingsStateService>().state
        myComponent?.data = state.autocompletionData.predictionSanitizerData
        myComponent?.liveAutocompletionEnabled = state.liveAutoCompletionEnabled
    }

    override fun disposeUIResources() {
        instance = null
        myComponent = null
    }

    companion object {
        var instance: SettingsConfigurable? = null
            private set
    }

}