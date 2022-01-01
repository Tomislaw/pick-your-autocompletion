package com.github.tomislaw.pickyourautocompletion.actions

import com.github.tomislaw.pickyourautocompletion.autocompletion.AutoCompletionService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service

class ApplySuggestionAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        if (e.presentation.isEnabled)
            e.project?.service<AutoCompletionService>()?.applyPrediction()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled =
            !e.project?.service<AutoCompletionService>()?.currentPrediction.isNullOrBlank()
        super.update(e)
    }
}