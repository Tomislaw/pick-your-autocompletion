package com.github.tomislaw.pickyourautocompletion.settings

import com.intellij.openapi.options.Configurable
import org.jetbrains.annotations.Nls
import javax.swing.JComponent

/**
 * Provides controller functionality for application settings.
 */
class SettingsConfigurable : Configurable {
    private var mySettingsComponent: SettingsComponent? = null

    // A default constructor with no arguments is required because this implementation
    // is registered as an applicationConfigurable EP
    override fun getDisplayName(): String {
        return "Pick Your Autocompletion"
    }

    override fun getPreferredFocusedComponent(): JComponent? {
        return mySettingsComponent?.preferredFocusedComponent
    }

    override fun createComponent(): JComponent? {
        mySettingsComponent = SettingsComponent()
        mySettingsComponent?.userNameText = SettingsState.instance.userId
        mySettingsComponent?.ideaUserStatus = SettingsState.instance.ideaStatus
        return mySettingsComponent?.panel
    }

    override fun isModified(): Boolean {
        val settings: SettingsState = SettingsState.instance
        var modified: Boolean = !mySettingsComponent?.userNameText.equals(settings.userId)
        modified = modified or (mySettingsComponent?.ideaUserStatus !== settings.ideaStatus)
        return modified
    }

    override fun apply() {
        val settings: SettingsState = SettingsState.instance
        settings.userId = mySettingsComponent?.userNameText ?: ""
        settings.ideaStatus = mySettingsComponent?.ideaUserStatus ?: false
    }

    override fun reset() {
        val settings: SettingsState = SettingsState.instance
        mySettingsComponent?.userNameText = settings.userId
        mySettingsComponent?.ideaUserStatus = settings.ideaStatus
    }

    override fun disposeUIResources() {
        mySettingsComponent = null
    }
}