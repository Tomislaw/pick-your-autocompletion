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
    private var mySettingsComponent: SettingsComponent? = null

    override fun getDisplayName(): String = "Pick Your Autocompletion"

    override fun getPreferredFocusedComponent(): JComponent? = mySettingsComponent?.preferredFocusedComponent

    override fun createComponent(): JComponent = SettingsComponent().apply {
        val state = service<SettingsStateService>().state
        this.data = state.autocompletionData.predictionSanitizerData
        this.liveAutocompletionEnabled = state.liveAutoCompletionEnabled
        mySettingsComponent = this
    }.panel

    override fun isModified(): Boolean {
        val state = service<SettingsStateService>().state
        return mySettingsComponent?.data != state.autocompletionData.predictionSanitizerData
                || mySettingsComponent?.liveAutocompletionEnabled != state.liveAutoCompletionEnabled
    }

    override fun apply() {
        service<SettingsStateService>().apply {
            state.autocompletionData.predictionSanitizerData = mySettingsComponent?.data ?: PredictionSanitizerData()
            state.liveAutoCompletionEnabled = mySettingsComponent?.liveAutocompletionEnabled ?: false
            settingsChanged()
        }
    }

    override fun reset() {
        val state = service<SettingsStateService>().state
        mySettingsComponent?.data = state.autocompletionData.predictionSanitizerData
        mySettingsComponent?.liveAutocompletionEnabled = state.liveAutoCompletionEnabled
    }

    override fun disposeUIResources() {
        mySettingsComponent = null
    }

}