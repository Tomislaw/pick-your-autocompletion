package com.github.tomislaw.pickyourautocompletion.autocompletion

import com.github.tomislaw.pickyourautocompletion.autocompletion.context.MultiFileContextBuilder
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.PredictionModeProvider
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.PredictionSanitizer
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.Predictor
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.webhook.WebhookPredictor
import com.github.tomislaw.pickyourautocompletion.settings.SettingsState
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project


class PredictorProviderService {

    private lateinit var predictor: Predictor
    private val contextBuilders = MultiFileContextBuilder() // todo get in from config

    private val stopProvider = PredictionModeProvider()
    private val predictionSanitizer = PredictionSanitizer()

    init {
        reload()
    }

    fun reload() {
        predictor = WebhookPredictor(SettingsState.instance.requestBuilder)
    }

    fun canPredict(project: Project, editor: Editor, offset: Int) =
        stopProvider.getPredictionMode(offset, editor, project).first != PredictionModeProvider.PredictMode.NONE

    fun predict(project: Project, editor: Editor, offset: Int): Iterator<String> {
        val (mode, stop) = stopProvider.getPredictionMode(offset, editor, project)

        if (mode == PredictionModeProvider.PredictMode.NONE)
            return iterator { }

        val context = contextBuilders.create(project, editor, offset)
        val tokenSize = if (mode == PredictionModeProvider.PredictMode.ONE_LINE) 50 else -1

        return iterator {
            while (true)
                yield(predictor
                    .predict(context, tokenSize, stop)
                    .let { predictionSanitizer.sanitize(editor, offset, it, stop) }
                )
        }
    }

    companion object {
        val instance: PredictorProviderService
            get() = ApplicationManager.getApplication().getService(PredictorProviderService::class.java)
    }
}


