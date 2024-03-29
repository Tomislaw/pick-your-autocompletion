package com.github.tomislaw.pickyourautocompletion.listeners

import com.github.tomislaw.pickyourautocompletion.autocompletion.AutoCompletionService
import com.github.tomislaw.pickyourautocompletion.settings.SettingsStateService
import com.github.tomislaw.pickyourautocompletion.settings.component.dialog.FirstUseDialog
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.startup.StartupActivity

class StartupListener : StartupActivity {
    override fun runActivity(project: Project) {

        project.service<AutoCompletionService>().startListening()

        val state = service<SettingsStateService>().state
        if (state.firstUse) {
            state.firstUse = false
            if (!FirstUseDialog(project).showAndGet())
                service<SettingsStateService>().settingsChanged()
        } else {
            service<SettingsStateService>().settingsChanged()
        }

    }
}