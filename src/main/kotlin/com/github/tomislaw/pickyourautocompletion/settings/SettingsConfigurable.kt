package com.github.tomislaw.pickyourautocompletion.settings

import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

/**
 * Provides controller functionality for application settings.
 */
class SettingsConfigurable : Configurable {
    private var mySettingsComponent: SettingsComponent? = null

    // A default constructor with no arguments is required because this implementation
    // is registered as an applicationConfigurable EP
    override fun getDisplayName(): String = "Pick Your Autocompletion"

    override fun getPreferredFocusedComponent(): JComponent? = mySettingsComponent?.preferredFocusedComponent

    override fun createComponent(): JComponent = SettingsComponent().apply {
        entryPoints.addAll(SettingsState.instance.entryPoints)
        mySettingsComponent = this
    }.panel

    override fun isModified(): Boolean {
        val modified = mySettingsComponent?.entryPoints != SettingsState.instance.entryPoints
        return modified
    }

    override fun apply() {
        SettingsState.instance.apply {
            this.entryPoints.clear()
            this.entryPoints.addAll(mySettingsComponent?.entryPoints ?: emptyList())
        }
    }

    override fun reset() {
        mySettingsComponent?.apply {
            entryPoints.clear()
            entryPoints.addAll(SettingsState.instance.entryPoints)
        }
    }

    override fun disposeUIResources() {
        mySettingsComponent = null
    }
}