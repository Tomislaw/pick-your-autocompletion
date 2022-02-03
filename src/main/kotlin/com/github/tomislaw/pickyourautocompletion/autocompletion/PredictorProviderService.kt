package com.github.tomislaw.pickyourautocompletion.autocompletion

import com.github.tomislaw.pickyourautocompletion.autocompletion.context.MultiFileContextBuilder
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.PredictionModeProvider
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.PredictionSanitizer
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.Predictor
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.webhook.WebhookPredictor
import com.github.tomislaw.pickyourautocompletion.listeners.AutocompletionStatusListener
import com.github.tomislaw.pickyourautocompletion.settings.SettingsState
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager
import java.net.MalformedURLException
import java.net.URL


class PredictorProviderService(private val project: Project) {

    private var predictor: Predictor? = null
    private val contextBuilders = MultiFileContextBuilder() // todo get in from config

    private val stopProvider = PredictionModeProvider()
    private val predictionSanitizer = PredictionSanitizer()

    init {
        reload()
    }

    fun reload() {

        if (!SettingsState.instance.requestBuilder.isConfigured)
            return project.messageBus.syncPublisher(AutocompletionStatusListener.TOPIC).onError(
                InvalidConfigurationError("Missing Request Builder configuration")
            )

        predictor = WebhookPredictor(SettingsState.instance.requestBuilder)
    }

    fun canPredict(editor: Editor, offset: Int) =
        predictor != null &&
                stopProvider.getPredictionMode(offset, editor, project).first != PredictionModeProvider.PredictMode.NONE

    fun predict(editor: Editor, offset: Int): Iterator<String> {
        val (mode, stop) = stopProvider.getPredictionMode(offset, editor, project)

        if (mode == PredictionModeProvider.PredictMode.NONE)
            return iterator { }

        val context = contextBuilders.create(project, editor, offset)
        val tokenSize = if (mode == PredictionModeProvider.PredictMode.ONE_LINE) 50 else -1

        return iterator {
            while (true)
                yield(predictor
                    ?.predict(context, tokenSize, stop)
                    ?.let { predictionSanitizer.sanitize(editor, offset, it, stop) } ?: ""
                )
        }
    }

    companion object {
        fun reloadConfig() {
            ProjectManager.getInstance().openProjects.forEach {
                it.service<PredictorProviderService>().reload()
            }
        }
    }
}


