package com.github.tomislaw.pickyourautocompletion.actions

import com.github.tomislaw.pickyourautocompletion.autocompletion.AutoCompletionService
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.components.service

class NextSuggestionAction : AnAction() {
    override fun actionPerformed(e: AnActionEvent) {
        e.project?.service<AutoCompletionService>()?.nextPrediction()
    }

    override fun update(e: AnActionEvent) {
        e.presentation.isEnabled =
            e.project?.service<AutoCompletionService>()?.canPredict ?: false
        super.update(e)
    }
}