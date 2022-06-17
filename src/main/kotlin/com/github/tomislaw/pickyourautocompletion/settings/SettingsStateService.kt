// Copyright 2000-2021 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.tomislaw.pickyourautocompletion.settings

import com.github.tomislaw.pickyourautocompletion.settings.data.PromptBuilder
import com.github.tomislaw.pickyourautocompletion.settings.data.RequestBuilder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.util.xmlb.XmlSerializerUtil

/**
 * Supports storing the application settings in a persistent way.
 * The [State] and [Storage] annotations define the name of the data and the file name where
 * these persistent application settings are stored.
 */
@State(
    name = "com.github.tomislaw.pickyourautocompletion.SettingsState",
    storages = [Storage("PickYourAutocompletion.xml")]
)
class SettingsStateService : PersistentStateComponent<SettingsStateService.State> {

    private var state = State()

    override fun getState(): State = state

    override fun loadState(state: State) {
        XmlSerializerUtil.copyBean(state, this.state)
    }

    data class State(
        var promptBuilder: PromptBuilder = PromptBuilder(),
        var requestBuilder: RequestBuilder = RequestBuilder(),
        var liveAutoCompletion: Boolean = false,
        var maxPredictionsInDialog: Int = 4
    )

    companion object {
        val instance: SettingsStateService
            get() = ApplicationManager.getApplication().getService(SettingsStateService::class.java)
    }
}