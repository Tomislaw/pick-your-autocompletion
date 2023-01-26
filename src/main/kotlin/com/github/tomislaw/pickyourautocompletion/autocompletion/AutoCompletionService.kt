package com.github.tomislaw.pickyourautocompletion.autocompletion

import com.github.tomislaw.pickyourautocompletion.settings.SettingsStateService
import com.github.tomislaw.pickyourautocompletion.ui.multiselect.MultiPredictionSelectWindow
import com.github.tomislaw.pickyourautocompletion.ui.visualiser.PredictionInlayVisualiser
import com.github.tomislaw.pickyourautocompletion.utils.firstLine
import com.github.tomislaw.pickyourautocompletion.utils.result
import com.intellij.openapi.Disposable
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.command.WriteCommandAction
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.editor.event.CaretEvent
import com.intellij.openapi.editor.event.CaretListener
import com.intellij.openapi.editor.event.DocumentEvent
import com.intellij.openapi.editor.event.DocumentListener
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.project.Project
import kotlinx.coroutines.*


class AutoCompletionService(private val project: Project) : Disposable {

    private var currentDocumentOffset = 0
    var currentPrediction = ""
        private set

    val canPredict: Boolean
        get() = ReadAction.compute<Boolean, RuntimeException> {
            val editor = FileEditorManager.getInstance(project).selectedTextEditor ?: return@compute false

            predictor.canPredict(editor, editor.caretModel.currentCaret.offset)
                    && !editor.selectionModel.hasSelection()
        }

    val hasPrevious: Boolean get() = canPredict && predictor.hasNext
    val hasNext: Boolean get() = canPredict && predictor.hasNext

    private lateinit var currentEditor: Editor

    private val visualiser = PredictionInlayVisualiser()
    private val predictor by lazy { project.getService(PredictorProviderService::class.java) }

    private val documentListener: DocumentListener = object : DocumentListener {
        override fun documentChanged(documentEvent: DocumentEvent) {

            // if live autocompletion is disabled and there isn't any prediction being shown then do nothing
            val state = service<SettingsStateService>().state
            synchronized(currentPrediction) {
                if (currentPrediction.isEmpty() && !state.liveAutoCompletionEnabled)
                    return
            }

            CoroutineScope(Dispatchers.EDT).launch {
                val editor = FileEditorManager.getInstance(project).selectedTextEditor
                val isHandlingPrediction = editor != null
                        && documentEvent.document == editor.document
                        && documentEvent.document.isWritable
                        && !documentEvent.isWholeTextReplaced

                // modify current prediction or request for new one based on changes in the document
                if (isHandlingPrediction)
                    handlePrediction(
                        editor!!, documentEvent.offset, documentEvent.newLength - documentEvent.oldLength,
                        "${documentEvent.newFragment}${documentEvent.oldFragment}"
                    )
            }
        }
    }

    private val caretListener: CaretListener = object : CaretListener {
        override fun caretPositionChanged(caretEvent: CaretEvent) {

            // if live autocompletion is disabled and there isn't any prediction being shown then do nothing
            val state = service<SettingsStateService>().state
            synchronized(currentPrediction) {
                if (currentPrediction.isEmpty() && !state.liveAutoCompletionEnabled)
                    return
            }

            if (caretEvent.editor.caretModel.currentCaret.offset == currentDocumentOffset)
                return

            CoroutineScope(Dispatchers.EDT).launch {
                val editor = FileEditorManager.getInstance(project).selectedTextEditor
                val isRemovingPrediction = editor == null
                        || caretEvent.editor != editor
                        || !caretEvent.editor.document.isWritable
                        || !canPredict

                if (isRemovingPrediction)
                    return@launch removePrediction()

                if (state.liveAutoCompletionEnabled) {
                    val newOffset = caretEvent.editor.caretModel.currentCaret.offset
                    synchronized(currentPrediction) {
                        // if there is no current prediction or caret moved to different position then create new prediction
                        if (currentPrediction.isBlank() || newOffset != currentDocumentOffset)
                            nextPrediction(caretEvent.editor, newOffset)
                    }
                }
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

        applyPrediction(currentDocumentOffset, currentPrediction, true)
    }

    private fun applyPrediction(offset: Int, prediction: String, forceOneLine: Boolean = false) {

        val applyString = if (forceOneLine) prediction.firstLine() else prediction
        WriteCommandAction.runWriteCommandAction(project) {
            currentEditor.document.insertString(offset, applyString)
            currentDocumentOffset = offset + applyString.length
            currentEditor.caretModel.currentCaret.moveToOffset(offset + applyString.length, true)
        }
    }

    fun nextPrediction() {
        val caret = ReadAction.compute<Int, Throwable> {
            currentEditor = FileEditorManager.getInstance(project).selectedTextEditor ?: return@compute -1
            return@compute currentEditor.caretModel.currentCaret.offset
        }
        if (caret != -1)
            nextPrediction(currentEditor, caret)
    }

    fun previousPrediction() {
        val caret = ReadAction.compute<Int, Throwable> {
            currentEditor = FileEditorManager.getInstance(project).selectedTextEditor ?: return@compute -1
            return@compute currentEditor.caretModel.currentCaret.offset
        }
        if (caret != -1)
            previousPrediction(currentEditor, caret)
    }

    fun multiplePrediction() {
        if (!canPredict)
            return

        val caret = ReadAction.compute<Int, Throwable> {
            currentEditor = FileEditorManager.getInstance(project).selectedTextEditor ?: return@compute -1
            return@compute currentEditor.caretModel.currentCaret.offset
        }
        if (caret == -1)
            return

        MultiPredictionSelectWindow(project).show { i, s -> applyPrediction(i, s, false) }
    }

    private fun nextPrediction(editor: Editor, offset: Int) {
        removePrediction()
        showProgressVisualisation(editor, offset)
        CoroutineScope(Dispatchers.Default).launch {
            predictor.nextPrediction(editor, offset).result().onSuccess {
                onPredictionReceived(PredictionEntry(offset, it, editor))
            }.onFailure {
                if (!predictor.isActive)
                    onPredictionReceived(PredictionEntry(offset, "", editor))
            }
        }
    }

    private fun previousPrediction(editor: Editor, offset: Int) {
        removePrediction()
        CoroutineScope(Dispatchers.Default).launch {
            predictor.previousPrediction(editor, offset).result().onSuccess {
                onPredictionReceived(PredictionEntry(offset, it, editor))
            }
        }
    }


    private fun onPredictionReceived(entry: PredictionEntry) {

        // set current prediction
        synchronized(currentPrediction) {
            if (entry.value.isEmpty())
                return removePrediction()

            currentPrediction = entry.value
            currentEditor = entry.editor
        }

        // if new prediction is not in the same place, remove previous one
        val sameCaret = ReadAction.compute<Boolean, Throwable> {
            return@compute currentEditor.caretModel.currentCaret.offset == entry.offset
        }

        if (sameCaret) handlePrediction(entry.editor, entry.offset, 0, "")
        else removePrediction()
    }

    private fun removePrediction() {
        synchronized(currentPrediction) { currentPrediction = "" }
        updateVisualisation()
    }

    private fun showProgressVisualisation(editor: Editor, offset: Int) {
        ApplicationManager.getApplication().invokeLater {
            visualiser.visualiseProgress(editor, offset)
        }
    }

    private fun updateVisualisation() {
        synchronized(currentPrediction) {
            if (currentPrediction.isEmpty())
                ApplicationManager.getApplication().invokeLater { visualiser.hide() }
            else
                ApplicationManager.getApplication().invokeLater {
                    visualiser.visualiseText(currentPrediction, currentEditor, currentDocumentOffset)
                }
        }

    }


    private fun handlePrediction(
        editor: Editor, offset: Int, change: Int, changedText: String
    ) = synchronized(currentPrediction) {
        val state = service<SettingsStateService>().state

        if (change < 0)
            removePrediction()

        val canUpdate = change >= 0
                && (offset == currentDocumentOffset || (offset + change) == currentDocumentOffset)
                && currentPrediction.startsWith(changedText)
                || changedText.isEmpty()

        // do not remove old prediction if change in document is corresponding to current prediction
        // update old one instead
        if (canUpdate) {
            currentDocumentOffset = offset + change
            currentPrediction = currentPrediction.drop(changedText.length)

            if (currentPrediction.isEmpty())
                nextPrediction(editor, currentDocumentOffset)

        } else {
            removePrediction()


            // caret change event is not called when removing characters, so we call it here
            if (change < 0) {
                if (canPredict && state.liveAutoCompletionEnabled)
                    nextPrediction(editor, editor.caretModel.currentCaret.offset)

            }
        }

        updateVisualisation()
    }

    override fun dispose() {
    }

    data class PredictionEntry(val offset: Int, val value: String, val editor: Editor)

}