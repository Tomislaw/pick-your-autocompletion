// Copyright 2000-2021 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.tomislaw.pickyourautocompletion.settings

import com.github.tomislaw.pickyourautocompletion.settings.data.ApiKey
import com.github.tomislaw.pickyourautocompletion.settings.data.EntryPoint
import com.github.tomislaw.pickyourautocompletion.settings.data.PromptBuilder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.containers.SortedList
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Supports storing the application settings in a persistent way.
 * The [State] and [Storage] annotations define the name of the data and the file name where
 * these persistent application settings are stored.
 */
@State(
    name = "com.github.tomislaw.pickyourautocompletion.SettingsState",
    storages = [Storage("SdkSettingsPlugin.xml")]
)
class SettingsState : PersistentStateComponent<SettingsState?> {
    val promptBuilders: MutableList<PromptBuilder> = mutableListOf()
    val passwords: MutableList<ApiKey> = mutableListOf()
    val entryPoints: MutableList<EntryPoint> = SortedList { o1, o2 -> o1.order.compareTo(o2.order) }
    var liveAutoCompletion = false

    override fun getState(): SettingsState = this

    override fun loadState(state: SettingsState) = XmlSerializerUtil.copyBean(state, this)

    companion object {
        val instance: SettingsState
            get() = ApplicationManager.getApplication().getService(SettingsState::class.java)
    }
}