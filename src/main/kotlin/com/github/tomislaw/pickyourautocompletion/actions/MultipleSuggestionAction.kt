package com.github.tomislaw.pickyourautocompletion.actions

import com.github.tomislaw.pickyourautocompletion.autocompletion.AutoCompletionService
import com.github.tomislaw.pickyourautocompletion.ui.multiselect.MultiPredictionSelectWindow
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service
import com.intellij.openapi.wm.RegisterToolWindowTask
import com.intellij.openapi.wm.ToolWindowManager
import kotlinx.coroutines.DelicateCoroutinesApi

@DelicateCoroutinesApi
class MultipleSuggestionAction : AnAction() {

    override fun actionPerformed(e: AnActionEvent) {
        if (e.project == null)
            return

        e.project?.service<AutoCompletionService>()?.multiplePrediction()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled =
            e.project?.service<AutoCompletionService>()?.canPredict ?: false
        super.update(e)
    }
}