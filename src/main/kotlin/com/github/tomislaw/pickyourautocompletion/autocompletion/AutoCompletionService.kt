package com.github.tomislaw.pickyourautocompletion.autocompletion

import com.github.tomislaw.pickyourautocompletion.ui.visualiser.PredictionInlayVisualiser
import com.github.tomislaw.pickyourautocompletion.settings.SettingsState
import com.github.tomislaw.pickyourautocompletion.ui.multiselect.MultiPredictionSelectWindow
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import kotlinx.coroutines.*
import java.util.concurrent.Executors


@DelicateCoroutinesApi
class AutoCompletionService(private val project: Project) : Disposable {

    private var predictionJob: Job? = null
    private var isRequestedPrediction = false
    private var currentDocumentOffset = 0

    var currentPrediction = ""
        private set
    var canPredict = false
        private set
    private lateinit var currentEditor: Editor

    private val visualiser = PredictionInlayVisualiser()
    private val predictor by lazy { project.getService(PredictorProviderService::class.java) }

    private val scope = Executors.newSingleThreadExecutor().asCoroutineDispatcher()

    private val documentListener: DocumentListener = object : DocumentListener {
        override fun documentChanged(documentEvent: DocumentEvent) {
            val editor = FileEditorManager.getInstance(project).selectedTextEditor

            if (editor == null
                || documentEvent.document != editor.document
                || !documentEvent.document.isWritable
                || documentEvent.isWholeTextReplaced
            ) return

            // modify current prediction or request for new one based on changes in the document
            handlePrediction(
                editor, documentEvent.offset, documentEvent.newLength - documentEvent.oldLength,
                "${documentEvent.newFragment}${documentEvent.oldFragment}"
            )
        }
    }

    private val caretListener: CaretListener = object : CaretListener {
        override fun caretPositionChanged(caretEvent: CaretEvent) = synchronized(currentPrediction) {
            val editor = FileEditorManager.getInstance(project).selectedTextEditor
            val newOffset = caretEvent.editor.caretModel.currentCaret.offset

            canPredict = editor?.caretModel?.currentCaret == caretEvent.caret
                    && predictor.canPredict(caretEvent.editor, newOffset)
                    && !caretEvent.editor.selectionModel.hasSelection()

            // when cannot predict any more then hide current prediction
            if (!canPredict)
                return removePrediction()

            // if live autocompletion is enabled then request new permission
            if (SettingsState.instance.liveAutoCompletion)
                synchronized(currentPrediction) {
                    // if there is no current prediction or caret moved to different position then create new prediction
                    if (currentPrediction.isBlank() || newOffset != currentDocumentOffset)
                        predict(caretEvent.editor, newOffset)
                }
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
        synchronized(currentPrediction) {
            if (currentPrediction.isBlank())
                return
        }

        putPrediction(currentDocumentOffset, currentPrediction)
    }

    fun nextPrediction() {
        val caret = ReadAction.compute<Int, Throwable> {
            return@compute currentEditor.caretModel.currentCaret.offset
        }
        predict(currentEditor, caret)
    }

    fun multiplePrediction() {
        if (!canPredict)
            return

        val offset = currentDocumentOffset
        MultiPredictionSelectWindow(project, suspend { predictor.predict(currentEditor, currentDocumentOffset) })
            .show { prediction ->
                putPrediction(offset, prediction)
            }
    }

    fun predict(editor: Editor, offset: Int) {
        removePrediction()

        synchronized(isRequestedPrediction) {
            when {
                // if old job is finished then schedule new job
                predictionJob == null || predictionJob?.isActive == false -> {

                    isRequestedPrediction = false
                    currentDocumentOffset = offset

                    predictionJob = requestPrediction(editor, offset)

                }
                // if old job is not finished then queue next job
                predictionJob?.isActive == true -> {
                    currentDocumentOffset = offset
                    isRequestedPrediction = true
                }
            }
        }
    }

    private fun putPrediction(offset: Int, prediction: String) {
        // insert on apply and move caret
        WriteCommandAction.runWriteCommandAction(project,
            Runnable {
                currentEditor.document.insertString(offset, prediction)
                currentEditor.caretModel.currentCaret.moveToOffset(offset + prediction.length)
                removePrediction()
            }
        )
    }

    private fun requestPrediction(editor: Editor, offset: Int) = GlobalScope.launch(scope) {
        val start = System.currentTimeMillis()

        val prediction = predictor.predict(editor, offset)

        // update current prediction
        synchronized(currentPrediction) {
            currentPrediction = prediction
            currentEditor = editor
        }

        // if new prediction is not in the same place, remove previous one
        val sameCaret = ReadAction.compute<Boolean, Throwable> {
            return@compute editor.caretModel.currentCaret.offset == offset
        }

        if (sameCaret) handlePrediction(editor, offset, 0, "")
        else removePrediction()

        // request prediction again if requested when previous prediction was not finished
        // todo cancel previous task instead of stacking them
        GlobalScope.launch(scope) {
            synchronized(isRequestedPrediction) {
                if (isRequestedPrediction)
                    predict(editor, currentDocumentOffset)
                println("Measured time for whole prediction: ${start - System.currentTimeMillis()}")
            }
        }
    }

    private fun removePrediction() {
        synchronized(currentPrediction) { currentPrediction = "" }
        ApplicationManager.getApplication().invokeLater { visualiser.hide() }
    }


    private fun handlePrediction(
        editor: Editor, offset: Int, change: Int, changedText: String
    ) = synchronized(currentPrediction) {
        val canUpdate = change >= 0
                && offset == currentDocumentOffset
                && currentPrediction.startsWith(changedText)
                || currentPrediction.isBlank()

        // do not remove old prediction if change in document is corresponding to current prediction
        // update old one instead
        if (canUpdate) {
            currentDocumentOffset = offset + change
            currentPrediction = currentPrediction.drop(changedText.length)
            ApplicationManager.getApplication().invokeLater {
                visualiser.visualise(currentPrediction, editor, currentDocumentOffset)
            }
        } else {
            removePrediction()

            // caret change event is not called when removing characters, so we call it here
            if (change < 0) {
                canPredict = predictor.canPredict(editor, offset)
                if (canPredict && SettingsState.instance.liveAutoCompletion)
                    predict(editor, editor.caretModel.currentCaret.offset)

            }
        }
    }

    override fun dispose() {
    }
}