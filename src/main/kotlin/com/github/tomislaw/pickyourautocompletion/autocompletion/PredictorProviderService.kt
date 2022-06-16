package com.github.tomislaw.pickyourautocompletion.autocompletion

import com.github.tomislaw.pickyourautocompletion.autocompletion.context.ContextBuilder
import com.github.tomislaw.pickyourautocompletion.autocompletion.context.SingleFileContextBuilder
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.DelayingTaskExecutor
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.PredictionModeProvider
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.PredictionSanitizer
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.Predictor
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.webhook.WebhookPredictor
import com.github.tomislaw.pickyourautocompletion.errors.MissingConfigurationError
import com.github.tomislaw.pickyourautocompletion.listeners.AutocompletionStatusListener
import com.github.tomislaw.pickyourautocompletion.settings.SettingsState
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManager


class PredictorProviderService(private val project: Project) {

    private var predictor: Predictor? = null
    private var contextBuilder: ContextBuilder? = null

    private val stopProvider = PredictionModeProvider()
    private val predictionSanitizer = PredictionSanitizer()

    private val taskExecutor = DelayingTaskExecutor<String>()


    fun reload() {
        if (!SettingsState.instance.requestBuilder.isConfigured) {
            project.messageBus
                .syncPublisher(AutocompletionStatusListener.TOPIC)
                .onError(MissingConfigurationError())
            return
        }

        predictor = WebhookPredictor(SettingsState.instance.requestBuilder)
        contextBuilder = SingleFileContextBuilder(SettingsState.instance.promptBuilder)
    }

    fun canPredict(editor: Editor, offset: Int) =
        predictor != null &&
                stopProvider.getPredictionMode(offset, editor, project).first != PredictionModeProvider.PredictMode.NONE


    fun predict(editor: Editor, offset: Int) = taskExecutor.scheduleTask(predictor?.delayTime() ?: 0) {

        if (predictor == null || contextBuilder == null) {
            project.messageBus
                .syncPublisher(AutocompletionStatusListener.TOPIC)
                .onError(MissingConfigurationError())
            throw MissingConfigurationError()
        }

        var time = System.currentTimeMillis()
        val (mode, stop) = stopProvider.getPredictionMode(offset, editor, project)
        println("get prediction mode time " + (System.currentTimeMillis() - time))

        if (mode == PredictionModeProvider.PredictMode.NONE)
            return@scheduleTask ""

        time = System.currentTimeMillis()
        val context = contextBuilder!!.create(project, editor, offset)
        println("get context time " + (System.currentTimeMillis() - time))

        val tokenSize = if (mode == PredictionModeProvider.PredictMode.ONE_LINE) 50 else -1

        return@scheduleTask predictor!!
            .let {

                val time = System.currentTimeMillis()
                val result = it.predict(context, tokenSize)
                println("request time " + (System.currentTimeMillis() - time))

                result
            }.mapCatching {

                val time = System.currentTimeMillis()
                val result = predictionSanitizer.sanitize(editor, offset, it, stop)
                println("prediction sanitize time " + (System.currentTimeMillis() - time))

                result
            }
            .onFailure {
                runCatching {
                    project.messageBus
                        .syncPublisher(AutocompletionStatusListener.TOPIC)
                        .onError(it)
                }
            }
            .getOrThrow()
    }

    companion object {
        fun reloadConfig() {
            ProjectManager.getInstance().openProjects.forEach {
                it.service<PredictorProviderService>().reload()
            }
        }
    }
}


