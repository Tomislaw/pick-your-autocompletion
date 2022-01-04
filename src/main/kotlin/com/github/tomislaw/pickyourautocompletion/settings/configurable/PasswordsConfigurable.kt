package com.github.tomislaw.pickyourautocompletion.settings.configurable

import com.github.tomislaw.pickyourautocompletion.settings.SettingsState
import com.github.tomislaw.pickyourautocompletion.settings.component.EntryPointsComponent
import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

/**
 * Provides controller functionality for application settings.
 */
class PasswordsConfigurable : Configurable {
    private var myEntryPointsComponent: EntryPointsComponent? = null

    // A default constructor with no arguments is required because this implementation
    // is registered as an applicationConfigurable EP
    override fun getDisplayName(): String = "Pick Your Autocompletion"

    override fun getPreferredFocusedComponent(): JComponent? = myEntryPointsComponent?.preferredFocusedComponent

    override fun createComponent(): JComponent = EntryPointsComponent().apply {
        entryPoints.addAll(SettingsState.instance.entryPoints)
        myEntryPointsComponent = this
    }.panel

    override fun isModified(): Boolean {
        val modified = myEntryPointsComponent?.entryPoints != SettingsState.instance.entryPoints
        return modified
    }

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