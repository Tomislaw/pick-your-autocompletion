package com.github.tomislaw.pickyourautocompletion.actions

import com.github.tomislaw.pickyourautocompletion.settings.SettingsState
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.ToggleAction

class ToggleLiveAutoCompletionAction : ToggleAction() {

    override fun isSelected(e: AnActionEvent): Boolean {
      return SettingsState.instance.liveAutoCompletion
    }

    override fun setSelected(e: AnActionEvent, state: Boolean) {
        SettingsState.instance.liveAutoCompletion = state
    }
}