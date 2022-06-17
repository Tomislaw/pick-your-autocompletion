package com.github.tomislaw.pickyourautocompletion.actions

import com.github.tomislaw.pickyourautocompletion.settings.SettingsStateService
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction

class ToggleLiveAutoCompletionAction : ToggleAction() {

    override fun isSelected(e: AnActionEvent): Boolean {
      return SettingsStateService.instance.state.liveAutoCompletion
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        SettingsStateService.instance.state.liveAutoCompletion = state
    }
}