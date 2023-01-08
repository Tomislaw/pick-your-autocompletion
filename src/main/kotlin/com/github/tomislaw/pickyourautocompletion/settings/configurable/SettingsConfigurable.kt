package com.github.tomislaw.pickyourautocompletion.settings.configurable

import com.github.tomislaw.pickyourautocompletion.settings.SettingsStateService
import com.github.tomislaw.pickyourautocompletion.settings.component.SettingsComponent
import com.intellij.openapi.components.service
import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

/**
 * Provides controller functionality for application settings.
 */
class SettingsConfigurable : Configurable  {
    private var mySettingsComponent: SettingsComponent? = null

    override fun getDisplayName(): String = "Pick Your Autocompletion"

    override fun getPreferredFocusedComponent(): JComponent? = mySettingsComponent?.preferredFocusedComponent

    override fun createComponent(): JComponent = SettingsComponent().panel

    override fun isModified(): Boolean = false

    override fun apply() {
        service<SettingsStateService>().settingsChanged()
    }

    override fun reset() {}

    override fun disposeUIResources() {
        mySettingsComponent = null
    }

}