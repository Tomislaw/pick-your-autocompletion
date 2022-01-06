package com.github.tomislaw.pickyourautocompletion.settings.configurable

import com.github.tomislaw.pickyourautocompletion.settings.SettingsState
import com.github.tomislaw.pickyourautocompletion.settings.component.EntryPointsComponent
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
        entryPoints.addAll(SettingsState.instance.entryPoints)
        myEntryPointsComponent = this
    }.panel

    override fun isModified(): Boolean = myEntryPointsComponent?.entryPoints != SettingsState.instance.entryPoints

    override fun apply() {
        SettingsState.instance.apply {
            this.entryPoints.clear()
            this.entryPoints.addAll(myEntryPointsComponent?.entryPoints ?: emptyList())
        }
    }

    override fun reset() {
        myEntryPointsComponent?.apply {
            entryPoints.clear()
            entryPoints.addAll(SettingsState.instance.entryPoints)
        }
    }

    override fun disposeUIResources() {
        myEntryPointsComponent = null
    }
}