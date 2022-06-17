package com.github.tomislaw.pickyourautocompletion.settings.configurable

import com.github.tomislaw.pickyourautocompletion.autocompletion.PredictorProviderService
import com.github.tomislaw.pickyourautocompletion.settings.SettingsStateService
import com.github.tomislaw.pickyourautocompletion.settings.component.RequestBuilderComponent
import com.github.tomislaw.pickyourautocompletion.settings.data.RequestBuilder
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
        instance = this@RequestBuilderConfigurable
        data = SettingsStateService.instance.state.requestBuilder
        myEntryPointsComponent = this
    }.panel

    override fun isModified(): Boolean = myEntryPointsComponent?.data != SettingsStateService.instance.state.requestBuilder

    override fun apply() {
        SettingsStateService.instance.apply {
            this.state.requestBuilder = myEntryPointsComponent?.data ?: RequestBuilder()
        }

        PredictorProviderService.reloadConfig()
    }

    override fun reset() {
        myEntryPointsComponent?.data = SettingsStateService.instance.state.requestBuilder
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