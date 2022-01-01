package com.github.tomislaw.pickyourautocompletion.autocompletion


import com.github.tomislaw.pickyourautocompletion.autocompletion.context.MyContextBuilder
import com.github.tomislaw.pickyourautocompletion.autocompletion.predictor.Predictor
import com.github.tomislaw.pickyourautocompletion.autocompletion.predictor.webhook.WebhookCodePredictor
import com.github.tomislaw.pickyourautocompletion.services.PredictionInlayVisualiser
import com.github.tomislaw.pickyourautocompletion.settings.SettingsState
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.util.EventDispatcher
import kotlinx.coroutines.*
import java.util.concurrent.Executors


@DelicateCoroutinesApi
class AutoCompletionService(private val project: Project) : Disposable {

    private var predictionJob: Job? = null
    private var isRequestedPrediction = false
    private var predictionOffset = 0
    var currentPrediction = ""
        private set
    var canPredict = false
        private set
    private lateinit var currentEditor: Editor

    private val visualiser = PredictionInlayVisualiser()
    private val contextBuilder = MyContextBuilder()
    private var predictor: Predictor = WebhookCodePredictor.DEFAULT

    private val scope = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    private val documentListener: DocumentListener = object : DocumentListener {
        override fun documentChanged(documentEvent: DocumentEvent) {
            val editor = FileEditorManager.getInstance(project).selectedTextEditor

            if (editor == null
                || documentEvent.document != editor.document
                || !documentEvent.document.isWritable
                || documentEvent.isWholeTextReplaced
            ) return

            // when removing character caret listener is not called

            handlePrediction(
                editor, documentEvent.offset, documentEvent.newLength - documentEvent.oldLength,
                "${documentEvent.newFragment}${documentEvent.oldFragment}"
            )
        }
    }

    private val caretListener: CaretListener = object : CaretListener {
        override fun caretPositionChanged(caretEvent: CaretEvent) = synchronized(currentPrediction) {
            val editor = FileEditorManager.getInstance(project).selectedTextEditor
            canPredict = editor?.caretModel?.currentCaret == caretEvent.caret
                    && canPredict(caretEvent.editor, caretEvent.editor.caretModel.primaryCaret.offset)

            if (!SettingsState.instance.liveAutoCompletion
                || !canPredict
            )
                return removePrediction()

            if (currentPrediction.isBlank() || editor?.caretModel?.currentCaret?.offset != predictionOffset)
                predict(
                    caretEvent.editor,
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

    fun applyPrediction() {
        if (currentPrediction.isBlank())
            return

        WriteCommandAction.runWriteCommandAction(project,
            Runnable {
                currentEditor.document.insertString(predictionOffset, currentPrediction)
                currentEditor.caretModel.currentCaret.moveToOffset(predictionOffset + currentPrediction.length)
                removePrediction()
            }
        )
    }

    fun predict(editor: Editor, offset: Int) {
        removePrediction()

        synchronized(isRequestedPrediction) {
            when {
                // if old job is finished then schedule new job
                predictionJob == null || predictionJob?.isActive == false -> {

                    isRequestedPrediction = false
                    predictionOffset = offset

                    predictionJob = GlobalScope.launch(scope) {
                        requestPrediction(editor, offset)
                    }
                }
                // if old job is not finished then queue next job
                predictionJob?.isActive == true -> {
                    predictionOffset = offset
                    isRequestedPrediction = true
                }
            }
        }
    }

    private fun requestPrediction(editor: Editor, offset: Int) {
        val context = contextBuilder.create(project, editor.document, offset)
        val prediction = predictor.predict(context)
        ApplicationManager.getApplication().invokeLater {
            synchronized(currentPrediction) {
                currentPrediction = prediction
                currentEditor = editor

                if (editor.caretModel.currentCaret.offset == offset) {
                    handlePrediction(editor, offset, 0, "")
                } else {
                    removePrediction()
                }
            }
        }

        // request prediction again if requested when previous prediction was not finished
        // todo cancel previous task instead of stacking them
        synchronized(isRequestedPrediction) {
            GlobalScope.launch {
                if (isRequestedPrediction)
                    predict(editor, predictionOffset)
            }
        }
    }

    private fun canPredict(editor: Editor, offset: Int): Boolean {
        // can edit file
        if (!editor.document.isWritable)
            return false

        // same editor as currently focused one
        if (editor != FileEditorManager.getInstance(project).selectedTextEditor)
            return false

        // not predicting after document size
        if (editor.document.textLength < offset)
            return false

        // is last in line
        val line = editor.document.getLineNumber(offset)
        val endLine = editor.document.getLineEndOffset(line)
        val lastInLine =
            editor.document.getText(TextRange(offset, (endLine + 1).coerceAtMost(editor.document.textLength))).let {
                it.startsWith("\n") or it.isBlank()
            }

        return lastInLine
    }

    private fun removePrediction() {
        currentPrediction = ""
        visualiser.hide()
    }


    private fun handlePrediction(
        editor: Editor, offset: Int, change: Int, changedText: String
    ) = synchronized(currentPrediction) {
        val canUpdate = change >= 0
                && offset == predictionOffset
                && currentPrediction.startsWith(changedText)
                || currentPrediction.isBlank()

        if (canUpdate) {
            predictionOffset = offset + change
            currentPrediction = currentPrediction.drop(changedText.length)
            visualiser.visualise(currentPrediction, editor, predictionOffset)
        } else {
            removePrediction()

            // caret change event is not called when removing characters, so we call it here
            if (change < 0) {
                canPredict = canPredict(editor, editor.caretModel.currentCaret.offset)
                if (canPredict && SettingsState.instance.liveAutoCompletion)
                    predict(editor, editor.caretModel.currentCaret.offset)

            }
        }
    }

    companion object {
        val instance: PredictorProviderService
            get() = ApplicationManager.getApplication().getService(PredictorProviderService::class.java)
    }

    override fun dispose() {
    }
}