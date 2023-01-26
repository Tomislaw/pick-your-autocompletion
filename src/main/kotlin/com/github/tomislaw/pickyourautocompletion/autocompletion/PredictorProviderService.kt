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
import kotlinx.coroutines.*


class PredictorProviderService(private val project: Project) {

    val hasNext get() = true
    val hasPrevious get() = predictionCache.hasPrevious()

    val isActive get() = taskExecutor.isActive

    private val sanitize = service<SettingsStateService>().state.autocompletionData.predictionSanitizerData


    private var predictor: Predictor? = null
    private var contextBuilder: ContextBuilder? = null

    private var stopProvider = PredictionStopTokenProvider(sanitize)

    private var predictionSanitizer = PredictionSanitizer(sanitize)

    private val taskExecutor = DelayingTaskExecutor()

    private val predictionCache = PredictionCache()

    private var maxTokensInSinglePrediction =
        service<SettingsStateService>().state.autocompletionData.maxTokensInSinglePrediction
    private var maxTokensInMultiPrediction =
        service<SettingsStateService>().state.autocompletionData.maxTokensInMultiPrediction
    private var maxPredictionsInDialog =
        service<SettingsStateService>().state.autocompletionData.maxPredictionsInDialog

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

        predictionSanitizer = PredictionSanitizer(sanitize)
        stopProvider = PredictionStopTokenProvider(sanitize)

        maxTokensInSinglePrediction =
            service<SettingsStateService>().state.autocompletionData.maxTokensInSinglePrediction
        maxTokensInMultiPrediction =
            service<SettingsStateService>().state.autocompletionData.maxTokensInMultiPrediction
        maxPredictionsInDialog =
            service<SettingsStateService>().state.autocompletionData.maxPredictionsInDialog
    }

    fun canPredict(editor: Editor, offset: Int) = predictor != null && stopProvider.getPredictionMode(offset, editor)
        .first != PredictionStopTokenProvider.PredictMode.NOT_AVAILABLE


    fun nextPrediction(editor: Editor, offset: Int): Deferred<String> {
        predictionCache.setEditorOffset(editor, offset)

        return if (predictionCache.hasNext()) {
            taskExecutor.scheduleTask {
                predictionCache.next()
            }
        } else {
            predictTask(editor, offset)
        }
    }

    fun multiplePredictions(editor: Editor, offset: Int) =
        if (predictor?.supportMultiple == true)
            generateSequence {
                val result = predictMultipleTask(editor, offset)
                val scope = CoroutineScope(Dispatchers.Default)
                sequence {
                    for (i in 0..maxPredictionsInDialog)
                        yield(scope.async { result.await()[i] })
                }
            }.flatten()
        else
            generateSequence {
                predictTask(editor, offset)
            }


    fun previousPrediction(editor: Editor, offset: Int): Deferred<String> = taskExecutor.scheduleTask {
        predictionCache.setEditorOffset(editor, offset)
        predictionCache.previous()
    }

    private fun predictTask(editor: Editor, offset: Int) = taskExecutor.scheduleTask(predictor?.delayTime ?: 0) {
        ensureValidConfiguration()
        val (mode, stop) = stopProvider.getPredictionMode(offset, editor)
        if (mode == PredictionStopTokenProvider.PredictMode.NOT_AVAILABLE) return@scheduleTask ""
        val context = contextBuilder!!.create(project, editor, offset)

        return@scheduleTask predictor!!.predict(context, maxTokensInSinglePrediction).mapCatching {
            sanitizeAndAddToCache(it, offset, stop, editor)
        }.onFailure {
            project.messageBus.syncPublisher(AutocompletionStatusListener.TOPIC).onError(it)
        }.getOrThrow()
    }

    private fun ensureValidConfiguration() {
        if (predictor == null || contextBuilder == null) {
            project.messageBus.syncPublisher(AutocompletionStatusListener.TOPIC)
                .onError(MissingConfigurationError())
            throw MissingConfigurationError()
        }
    }

    private fun sanitizeAndAddToCache(text: String, offset: Int, stop: List<String>, editor: Editor): String {
        val sanitizedPrediction = predictionSanitizer.sanitize(text, stop)
        predictionCache.setEditorOffset(editor, offset)
        predictionCache.add(sanitizedPrediction)
        return sanitizedPrediction
    }

    private fun predictMultipleTask(editor: Editor, offset: Int) =
        taskExecutor.scheduleTask(predictor?.delayTime ?: 0) {
            ensureValidConfiguration()

            val (mode, stop) = stopProvider.getPredictionMode(offset, editor)
            if (mode == PredictionStopTokenProvider.PredictMode.NOT_AVAILABLE) return@scheduleTask emptyList()
            val context = contextBuilder!!.create(project, editor, offset)

            return@scheduleTask predictor!!.predictMultiple(
                context, maxPredictionsInDialog, maxTokensInMultiPrediction, stop
            ).mapCatching { list ->
                list.map { sanitizeAndAddToCache(it, offset, stop, editor) }
            }.onFailure {
                project.messageBus.syncPublisher(AutocompletionStatusListener.TOPIC).onError(it)
            }.getOrThrow()
        }
}


