package com.github.tomislaw.pickyourautocompletion.settings.configurable

import com.github.tomislaw.pickyourautocompletion.settings.SettingsStateService
import com.github.tomislaw.pickyourautocompletion.settings.component.builders.RequestBuilderComponent
import com.github.tomislaw.pickyourautocompletion.settings.data.AutocompletionData
import com.github.tomislaw.pickyourautocompletion.settings.data.BuiltInRequestBuilderData
import com.github.tomislaw.pickyourautocompletion.settings.data.WebRequestBuilderData
import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

/**
 * Provides controller functionality for application settings.
 */
class RequestBuilderConfigurable : Configurable {
    private var myEntryPointsComponent: RequestBuilderComponent? = null

    override fun getDisplayName(): String = "Request Builder"

    override fun getPreferredFocusedComponent(): JComponent? = myEntryPointsComponent?.preferredFocusedComponent

    override fun createComponent(): JComponent = RequestBuilderComponent().apply {
        val state = service<SettingsStateService>().state.autocompletionData
        instance = this@RequestBuilderConfigurable
        webRequestData = state.webRequestBuilderData
        builtInRequestData = state.builtInRequestBuilderData
        type = state.builderType
        myEntryPointsComponent = this
    }.panel

    override fun isModified(): Boolean {
        val state = service<SettingsStateService>().state.autocompletionData
        return myEntryPointsComponent?.webRequestData != state.webRequestBuilderData
                || myEntryPointsComponent?.builtInRequestData != state.builtInRequestBuilderData
                || myEntryPointsComponent?.type != state.builderType
    }

    override fun apply() {
        service<SettingsStateService>().apply {
            this.state.autocompletionData.webRequestBuilderData =
                myEntryPointsComponent?.webRequestData ?: WebRequestBuilderData()
            this.state.autocompletionData.builtInRequestBuilderData =
                myEntryPointsComponent?.builtInRequestData ?: BuiltInRequestBuilderData()
            this.state.autocompletionData.builderType =
                myEntryPointsComponent?.type ?: AutocompletionData.BuilderType.Web
            settingsChanged()
        }
    }

    override fun reset() {
        myEntryPointsComponent?.webRequestData =
            service<SettingsStateService>().state.autocompletionData.webRequestBuilderData
    }

    override fun disposeUIResources() {
        myEntryPointsComponent = null
        instance = null
    }

    companion object {
        var instance: RequestBuilderConfigurable? = null
            private set
    }
}