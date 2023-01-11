package com.github.tomislaw.pickyourautocompletion.actions

import com.github.tomislaw.pickyourautocompletion.listeners.AutocompletionStatusListener
import com.github.tomislaw.pickyourautocompletion.settings.SettingsStateService
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction
import com.intellij.openapi.components.service

class ToggleLiveAutoCompletionAction : ToggleAction() {

    override fun isSelected(e: AnActionEvent): Boolean {
        val state = service<SettingsStateService>().state
        return state.liveAutoCompletionEnabled
    }

    override fun setSelected(e: AnActionEvent, newState: Boolean) {
        val state = service<SettingsStateService>().state
        state.liveAutoCompletionEnabled = newState
        e.project?.messageBus?.syncPublisher(AutocompletionStatusListener.TOPIC)?.onLiveAutocompletionChanged()
    }
}