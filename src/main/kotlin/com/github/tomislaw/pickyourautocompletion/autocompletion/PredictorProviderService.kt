package com.github.tomislaw.pickyourautocompletion.autocompletion

import com.github.tomislaw.pickyourautocompletion.autocompletion.context.ContextBuilder
import com.github.tomislaw.pickyourautocompletion.autocompletion.context.MultiFileContextBuilder
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.Predictor
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.PredictionModeProvider
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.PredictionSanitizer
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.webhook.WebhookPredictor
import com.github.tomislaw.pickyourautocompletion.settings.SettingsState
import com.github.tomislaw.pickyourautocompletion.settings.data.integrations.WebhookIntegration
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project


class PredictorProviderService {

    private val predictors = mutableListOf<Predictor>()
    private val contextBuilders = mutableListOf<ContextBuilder>()
    private val stopProvider = PredictionModeProvider()
    private val predictionSanitizer = PredictionSanitizer()

    init {
        reload()
    }

    fun reload() {
        predictors.clear()
        contextBuilders.clear()
        contextBuilders.add(MultiFileContextBuilder())
        SettingsState.instance.entryPoints.forEach {
            when (it) {
                is WebhookIntegration -> predictors.add(WebhookPredictor(it))
            }
        }
    }

    fun canPredict(project: Project, editor: Editor, offset: Int) =
        stopProvider.getPredictionMode(offset, editor, project).first != PredictionModeProvider.PredictMode.NONE

    fun predict(project: Project, editor: Editor, offset: Int): String {
        val (mode, stop) = stopProvider.getPredictionMode(offset, editor, project)

        if (mode == PredictionModeProvider.PredictMode.NONE)
            return ""

        val context = contextBuilders.first().create(project, editor, offset)
        val tokenSize = if (mode == PredictionModeProvider.PredictMode.ONE_LINE) 50 else -1

        return predictors.first()
            .predict(context, tokenSize, stop)
            .let { predictionSanitizer.sanitize(it, stop) }
    }


    companion object {
        val instance: PredictorProviderService
            get() = ApplicationManager.getApplication().getService(PredictorProviderService::class.java)
    }
}
