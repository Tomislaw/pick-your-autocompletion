package com.github.tomislaw.pickyourautocompletion.settings.data

import com.intellij.credentialStore.CredentialAttributes
import com.intellij.credentialStore.Credentials
import com.intellij.credentialStore.generateServiceName
import com.intellij.ide.passwordSafe.PasswordSafe

data class ApiKey(val name: String, val attributes: CredentialAttributes) {
    val password get() = PasswordSafe.instance.getPassword(attributes) ?: ""
    val id = "pwd." + name.lowercase().replace("\\s".toRegex(),"_")

    companion object {
        private const val SERVICE = "pickyourautocompletion"

        fun create(name: String, password: String): ApiKey {
            val attributes = CredentialAttributes(generateServiceName(SERVICE, name))
            PasswordSafe.instance.set(attributes, Credentials(SERVICE, password))
            return ApiKey(name, attributes)
        }
    }
}