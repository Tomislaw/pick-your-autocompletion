package com.github.tomislaw.pickyourautocompletion.settings.configurable

import com.github.tomislaw.pickyourautocompletion.autocompletion.PredictorProviderService
import com.github.tomislaw.pickyourautocompletion.settings.SettingsState
import com.github.tomislaw.pickyourautocompletion.settings.component.EntryPointsComponent
import com.github.tomislaw.pickyourautocompletion.settings.data.integrations.WebhookIntegration
import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

/**
 * Provides controller functionality for application settings.
 */
class EntryPointsConfigurable : Configurable {
    private var myEntryPointsComponent: EntryPointsComponent? = null

    override fun getDisplayName(): String = "Pick Your Autocompletion"

    override fun getPreferredFocusedComponent(): JComponent? = myEntryPointsComponent?.preferredFocusedComponent

    override fun createComponent(): JComponent = EntryPointsComponent().apply {
        instance = this@EntryPointsConfigurable
        entryPoints.addAll(SettingsState.instance.entryPoints.map { (it as WebhookIntegration).copy()})
        myEntryPointsComponent = this
    }.panel

    override fun isModified(): Boolean = myEntryPointsComponent?.entryPoints != SettingsState.instance.entryPoints

    override fun apply() {
        SettingsState.instance.apply {
            this.entryPoints.clear()
            this.entryPoints.addAll(myEntryPointsComponent?.entryPoints ?: emptyList())
        }
        PredictorProviderService.instance.reload()
    }

    override fun reset() {
        myEntryPointsComponent?.apply {
            entryPoints.clear()
            entryPoints.addAll(SettingsState.instance.entryPoints)
        }
    }

    override fun disposeUIResources() {
        myEntryPointsComponent = null
        instance = null
    }

    companion object {
        var instance: EntryPointsConfigurable? = null
            private set
    }
}