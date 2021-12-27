package com.github.tomislaw.pickyourautocompletion.autocompletion


import com.github.tomislaw.pickyourautocompletion.autocompletion.context.MyContextBuilder
import com.github.tomislaw.pickyourautocompletion.autocompletion.predictor.Predictor
import com.github.tomislaw.pickyourautocompletion.autocompletion.predictor.webhook.WebhookCodePredictor
import com.github.tomislaw.pickyourautocompletion.services.PredictionInlayVisualiser
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileDocumentManager
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import kotlinx.coroutines.*
import java.util.concurrent.Executors

class AutoCompletionService(private val project: Project) : Disposable {

    private var predictionJob: Job? = null
    private var isRequestedPrediction = false
    private var predictionOffset = 0
    private var currentPrediction = ""

    private val visualiser = PredictionInlayVisualiser()
    private val contextBuilder = MyContextBuilder()
    private var predictor: Predictor = WebhookCodePredictor.DEFAULT

    private val scope = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    private val documentListener: DocumentListener = object : DocumentListener {
        override fun documentChanged(documentEvent: DocumentEvent) {
            documentEvent.apply {
                if (!canPredict(documentEvent.document))
                    return

                handlePrediction(
                    document, offset, newLength - oldLength,
                    "${documentEvent.newFragment}${documentEvent.oldFragment}"
                )
            }
        }
    }

    private val caretListener: CaretListener = object : CaretListener {
        override fun caretPositionChanged(caretEvent: CaretEvent) = synchronized(currentPrediction) {
            if (caretEvent.editor.caretModel.primaryCaret != caretEvent.caret)
                return

            if (caretEvent.editor.caretModel.primaryCaret.offset != predictionOffset)
                predict(
                    caretEvent.editor.document,
                    caretEvent.editor.caretModel.primaryCaret.offset
                )
        }
    }

    fun startListening() {
        EditorFactory.getInstance().eventMulticaster.addDocumentListener(documentListener, this)
        EditorFactory.getInstance().eventMulticaster.addCaretListener(caretListener, this)
    }

    fun stopListening() {
        EditorFactory.getInstance().eventMulticaster.removeDocumentListener(documentListener)
        EditorFactory.getInstance().eventMulticaster.removeCaretListener(caretListener)
    }

    fun predict(document: Document, offset: Int) {
        removePrediction()

        synchronized(isRequestedPrediction) {
            when {
                // if old job is finished then schedule new job
                predictionJob == null || predictionJob?.isActive == false -> {

                    isRequestedPrediction = false
                    predictionOffset = offset

                    predictionJob = GlobalScope.launch(scope) {
                        requestPrediction(document, offset)
                    }
                }
                predictionJob?.isActive == true -> {
                    predictionOffset = offset
                    isRequestedPrediction = true
                }
            }
        }
    }

    private fun requestPrediction(document: Document, offset: Int) {
        val context = contextBuilder.create(project, document, offset)
        val prediction = predictor.predict(context)
        ApplicationManager.getApplication().invokeLater {
            synchronized(currentPrediction) {
                currentPrediction = prediction
                val editor = FileEditorManager.getInstance(project).selectedTextEditor
                if (editor != null && editor.caretModel.currentCaret.offset == offset) {
                    handlePrediction(document, offset, 0, "")
                } else {
                    removePrediction()
                }
            }
        }

        // request prediction again if requested when precious prediction was not finished
        synchronized(isRequestedPrediction) {
            GlobalScope.launch {
                if (isRequestedPrediction)
                    predict(document, predictionOffset)
            }
        }
    }

    private fun canPredict(document: Document): Boolean {
        val editor = FileEditorManager.getInstance(project).selectedTextEditor
        val incorrectFileWasUpdated = editor == null || editor.document != document
        return !incorrectFileWasUpdated
    }

    private fun removePrediction() {
        visualiser.hide()
    }


    private fun handlePrediction(
        document: Document, offset: Int, change: Int, changedText: String
    ) = synchronized(currentPrediction) {

        val editor = FileEditorManager.getInstance(project).selectedTextEditor
        if (!canPredict(document)) {
            currentPrediction = ""
            return removePrediction()
        }

        val canUpdate = change >= 0
                && offset == predictionOffset
                && currentPrediction.startsWith(changedText)

        if (canUpdate) {
            predictionOffset = offset + change
            currentPrediction = currentPrediction.drop(changedText.length)
            visualiser.visualise(currentPrediction, editor!!, predictionOffset)
        }
    }

    override fun dispose() {
    }
}