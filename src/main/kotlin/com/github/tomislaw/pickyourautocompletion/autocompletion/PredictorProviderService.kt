package com.github.tomislaw.pickyourautocompletion.autocompletion

import com.github.tomislaw.pickyourautocompletion.autocompletion.context.ContextBuilder
import com.github.tomislaw.pickyourautocompletion.autocompletion.context.SingleFileContextBuilder
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.*
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.onnx.OnnxPredictor
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.webhook.WebhookPredictor
import com.github.tomislaw.pickyourautocompletion.errors.MissingConfigurationError
import com.github.tomislaw.pickyourautocompletion.listeners.AutocompletionStatusListener
import com.github.tomislaw.pickyourautocompletion.settings.SettingsStateService
import com.github.tomislaw.pickyourautocompletion.settings.data.AutocompletionData
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import java.util.concurrent.Future


class PredictorProviderService(private val project: Project) {

    val hasNext get() = true
    val hasPrevious get() = predictionCache.hasPrevious()

    private val sanitize = service<SettingsStateService>().state.autocompletionData.predictionSanitizerData

    private var predictor: Predictor? = null
    private var contextBuilder: ContextBuilder? = null

    private val stopProvider = PredictionModeProvider(sanitize)

    private val predictionSanitizer = PredictionSanitizer(sanitize)

    private val taskExecutor = DelayingTaskExecutor<String>()

    private val predictionCache = PredictionCache()

    fun reload() {
        val state = service<SettingsStateService>().state.autocompletionData

        if (!state.isConfigured) {
            project.messageBus.syncPublisher(AutocompletionStatusListener.TOPIC).onError(MissingConfigurationError())
            return
        }

        predictor = when (state.builderType) {
            AutocompletionData.BuilderType.Web -> WebhookPredictor(state.webRequestBuilderData)
            AutocompletionData.BuilderType.BuiltIn -> OnnxPredictor(state.builtInRequestBuilderData)
        }

        contextBuilder = SingleFileContextBuilder(state.promptBuilderData)
    }

    fun canPredict(editor: Editor, offset: Int) = predictor != null && stopProvider.getPredictionMode(
        offset,
        editor,
        project
    ).first != PredictionModeProvider.PredictMode.NONE


    fun nextPrediction(editor: Editor, offset: Int): Future<String> = if (predictionCache.hasNext()) {
        taskExecutor.scheduleTask {
            predictionCache.setEditorOffset(editor, offset)
            predictionCache.next()
        }
    } else {
        predict(editor, offset)
    }

    fun previousPrediction(editor: Editor, offset: Int): Future<String> = taskExecutor.scheduleTask {
        predictionCache.setEditorOffset(editor, offset)
        predictionCache.previous()
    }

    private fun predict(editor: Editor, offset: Int) = taskExecutor.scheduleTask(predictor?.delayTime() ?: 0) {

        if (predictor == null || contextBuilder == null) {
            project.messageBus.syncPublisher(AutocompletionStatusListener.TOPIC).onError(MissingConfigurationError())
            throw MissingConfigurationError()
        }

        val (mode, stop) = stopProvider.getPredictionMode(offset, editor, project)
        if (mode == PredictionModeProvider.PredictMode.NONE) return@scheduleTask ""

        val context = contextBuilder!!.create(project, editor, offset)
        val tokenSize = if (mode == PredictionModeProvider.PredictMode.ONE_LINE) 10 else 20

        return@scheduleTask predictor!!.predict(context, tokenSize).mapCatching {
            val sanitizedPrediction = predictionSanitizer.sanitize(editor, offset, it, stop)
            predictionCache.setEditorOffset(editor, offset)
            predictionCache.add(sanitizedPrediction)
            sanitizedPrediction
        }.onFailure {
            runCatching {
                project.messageBus.syncPublisher(AutocompletionStatusListener.TOPIC).onError(it)
            }
        }.getOrThrow()
    }

}


