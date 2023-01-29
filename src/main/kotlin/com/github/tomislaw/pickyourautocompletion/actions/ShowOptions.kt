package com.github.tomislaw.pickyourautocompletion.actions

import com.github.tomislaw.pickyourautocompletion.settings.configurable.SettingsConfigurable
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.options.ShowSettingsUtil

class ShowOptions : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        ShowSettingsUtil.getInstance().showSettingsDialog(e.project, SettingsConfigurable::class.java)
    }
}