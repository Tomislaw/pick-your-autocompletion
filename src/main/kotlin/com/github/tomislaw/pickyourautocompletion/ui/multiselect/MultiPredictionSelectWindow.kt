package com.github.tomislaw.pickyourautocompletion.ui.multiselect

import com.github.tomislaw.pickyourautocompletion.PickYourAutocompletionIcons
import com.github.tomislaw.pickyourautocompletion.settings.SettingsState
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.EditorFactory
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
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.launch
import javax.swing.*


@Suppress("UnstableApiUsage")
class MultiPredictionSelectWindow(
    private val project: Project,
    private val predictions: Iterator<String>
) {

    private val ID = "MultiPredictionSelectWindow"
    private val maxPredictions = SettingsState.instance.maxPredictionsInDialog
    private var predictionsCount = 0

    private var toolWindow: ToolWindow? = null
    private lateinit var onSelect: (String) -> Unit

    private fun getPredictionPanel(prediction: String, id: Int) = panel {
        separator("Prediction $id")
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
                onSelect.invoke(prediction)
                toolWindow?.hide()
            }
        }.bottomGap(BottomGap.SMALL)
    }

    private val completions = JPanel().apply { layout = BoxLayout(this, BoxLayout.Y_AXIS) }

    private val content = panel {
        row {
            button("Refresh") { GlobalScope.launch { updateCompletions() } }
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

        while (predictionsCount < maxPredictions && predictions.hasNext()) {
            checkCanceled()
            val prediction = predictions.next()
            val predictionId = predictionsCount + 1
            ApplicationManager.getApplication().invokeLater {
                completions.add(getPredictionPanel(prediction, predictionId))
            }
            predictionsCount++
        }
        ApplicationManager.getApplication().invokeLater {
            completions.remove(loading)
            completions.revalidate()
            completions.repaint()
        }
    }

    fun show(onSelect: (String) -> Unit) {
        this.onSelect = onSelect

        // get or create tool window
        val toolWindowManager = ToolWindowManager.getInstance(project)
        toolWindow = toolWindowManager.getToolWindow(ID) ?: toolWindowManager.registerToolWindow(
            RegisterToolWindowTask(
                id = ID,
                anchor = ToolWindowAnchor.RIGHT,
                icon = PickYourAutocompletionIcons.LogoAction,
                canCloseContent = true,
                sideTool = true
            )
        )
        toolWindow!!.setToHideOnEmptyContent(true)

        // add content to tool window
        val contentFactory = ContentFactory.SERVICE.getInstance()
        val content: Content = contentFactory.createContent(this.content, ID, false)
        toolWindow!!.component.putClientProperty(ToolWindowContentUi.HIDE_ID_LABEL, "true")
        toolWindow!!.contentManager.removeAllContents(true)
        toolWindow!!.contentManager.addContent(content)

        toolWindow!!.show()

        GlobalScope.launch { updateCompletions() }
    }

    fun hide() {
        toolWindow?.hide()
    }

}