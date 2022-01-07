package com.github.tomislaw.pickyourautocompletion.settings.configurable

import com.github.tomislaw.pickyourautocompletion.autocompletion.PredictorProviderService
import com.github.tomislaw.pickyourautocompletion.settings.SettingsState
import com.github.tomislaw.pickyourautocompletion.settings.component.PasswordsComponent
import com.intellij.openapi.options.Configurable
import javax.swing.JComponent

/**
 * Provides controller functionality for application settings.
 */
class PasswordsConfigurable : Configurable {

    private var myPasswordsComponent: PasswordsComponent? = null

    // A default constructor with no arguments is required because this implementation
    // is registered as an applicationConfigurable EP
    override fun getDisplayName(): String = "Passwords and Api Keys"

    override fun getPreferredFocusedComponent(): JComponent? = myPasswordsComponent?.preferredFocusedComponent

    override fun createComponent(): JComponent = PasswordsComponent().apply {
        instance = this@PasswordsConfigurable
        apiKeys.addAll(SettingsState.instance.passwords)
        myPasswordsComponent = this
    }.panel

    override fun isModified(): Boolean {
        val modified = myPasswordsComponent?.apiKeys != SettingsState.instance.passwords
        return modified
    }

    override fun apply() {
        SettingsState.instance.apply {
            this.passwords.clear()
            this.passwords.addAll(myPasswordsComponent?.apiKeys ?: emptyList())
        }
        PredictorProviderService.instance.reload()
    }

    override fun reset() {
        myPasswordsComponent?.apply {
            apiKeys.clear()
            apiKeys.addAll(SettingsState.instance.passwords)
        }
    }

    override fun disposeUIResources() {
        myPasswordsComponent = null
        instance
    }

    companion object {
        var instance: PasswordsConfigurable? = null
            private set
    }
}