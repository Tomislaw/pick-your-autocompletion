package com.github.tomislaw.pickyourautocompletion.ui.multiselect

import com.github.tomislaw.pickyourautocompletion.Icons
import com.github.tomislaw.pickyourautocompletion.autocompletion.AutoCompletionService
import com.github.tomislaw.pickyourautocompletion.autocompletion.PredictorProviderService
import com.github.tomislaw.pickyourautocompletion.settings.SettingsStateService
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.EDT
import com.intellij.openapi.components.service
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.EditorFactory
import com.intellij.openapi.fileEditor.FileEditorManager
import com.intellij.openapi.fileTypes.FileTypes
import com.intellij.openapi.progress.checkCanceled
import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.RegisterToolWindowTask
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowAnchor
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.openapi.wm.impl.content.ToolWindowContentUi
import com.intellij.ui.AnimatedIcon
import com.intellij.ui.EditorTextField
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.content.Content
import com.intellij.ui.content.ContentFactory
import com.intellij.ui.dsl.builder.BottomGap
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.dsl.gridLayout.VerticalAlign
import kotlinx.coroutines.*
import javax.swing.*


@Suppress("UnstableApiUsage")
class MultiPredictionSelectWindow(
    private val project: Project,
) {

    private val ID = "Multi prediction"
    private val maxPredictions = service<SettingsStateService>().state.autocompletionData.maxPredictionsInDialog
    private var predictionsCount = 0

    private var toolWindow: ToolWindow? = null
    private lateinit var onSelect: (Int, String) -> Unit
    private val EDT = CoroutineScope(Dispatchers.EDT)

    private fun getPredictionPanel(prediction: String, offset: Int) = panel {
        separator()
        row {
            cell(
                EditorTextField(
                    EditorFactory.getInstance().createDocument(prediction),
                    project,
                    FileTypes.PLAIN_TEXT,
                    true,
                    false
                )
            ).horizontalAlign(HorizontalAlign.FILL).verticalAlign(VerticalAlign.FILL)
        }
        row {
            button("Apply Completion") {
                onSelect.invoke(offset, prediction)
                toolWindow?.hide()
            }
        }.bottomGap(BottomGap.SMALL)
    }

    private val completions = JPanel().apply { layout = BoxLayout(this, BoxLayout.Y_AXIS) }

    private val content = panel {
        row {
            button("Refresh") { CoroutineScope(Dispatchers.Default).launch { updateCompletions() } }
        }.bottomGap(BottomGap.MEDIUM)
        row {
            cell(JBScrollPane(completions))
                .horizontalAlign(HorizontalAlign.FILL)
                .verticalAlign(VerticalAlign.FILL)
        }.resizableRow()

    }

    private suspend fun updateCompletions() {
        predictionsCount = 0
        val loading = JLabel("Loading...", AnimatedIcon.Default(), SwingConstants.LEFT)
        ApplicationManager.getApplication().invokeLater {
            completions.removeAll()
            completions.add(loading)
            completions.revalidate()
            completions.repaint()
        }

        val autocompletion = project.service<AutoCompletionService>()
        val editor: Editor? = EDT.async { FileEditorManager.getInstance(project).selectedTextEditor }.await()
        val canPredict = EDT.async { autocompletion.canPredict }.await()
        if (editor != null && canPredict) {
            val completionProvider = project.service<PredictorProviderService>()
            val offset = EDT.async { editor.caretModel.offset }.await()
            val iterator = completionProvider.multiplePredictions(editor, offset).iterator()
            while (predictionsCount < maxPredictions) {
                checkCanceled()
                runCatching { iterator.next().await() }.getOrNull()?.apply {
                    ApplicationManager.getApplication().invokeLater {
                        completions.add(getPredictionPanel(this, offset))
                    }
                }
                predictionsCount++
            }
        }
        ApplicationManager.getApplication().invokeLater {
            completions.remove(loading)
            completions.revalidate()
            completions.repaint()
        }
    }

    fun show(onSelect: (Int, String) -> Unit) {
        this.onSelect = onSelect

        // get or create tool window
        val toolWindowManager = ToolWindowManager.getInstance(project)
        toolWindow = toolWindowManager.getToolWindow(ID) ?: toolWindowManager.registerToolWindow(
            RegisterToolWindowTask(
                id = ID,
                anchor = ToolWindowAnchor.RIGHT,
                icon = Icons.LogoAction,
                canCloseContent = true,
                sideTool = true
            )
        )
        toolWindow!!.setToHideOnEmptyContent(true)

        // add content to tool window
        val contentFactory = ContentFactory.getInstance()
        val content: Content = contentFactory.createContent(this.content, ID, false)
        toolWindow!!.component.putClientProperty(ToolWindowContentUi.HIDE_ID_LABEL, "true")
        toolWindow!!.contentManager.removeAllContents(true)
        toolWindow!!.contentManager.addContent(content)

        toolWindow!!.show()

        CoroutineScope(Dispatchers.Default).launch { updateCompletions() }
    }

    fun hide() {
        toolWindow?.hide()
    }

}