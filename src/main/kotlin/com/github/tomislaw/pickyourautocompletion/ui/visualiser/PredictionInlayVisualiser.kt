package com.github.tomislaw.pickyourautocompletion.ui.visualiser

import com.github.tomislaw.pickyourautocompletion.ui.visualiser.presentation.MyInlayFactory
import com.intellij.codeInsight.hint.HintManager
import com.intellij.codeInsight.hints.InlayPresentationFactory.HoverListener
import com.intellij.codeInsight.hints.presentation.*
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.ActionManager
import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.keymap.KeymapUtil
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.dsl.builder.*
import java.awt.Point
import java.awt.event.MouseEvent

@Suppress("UnstableApiUsage")
class PredictionInlayVisualiser {
    private val inlays = mutableListOf<Inlay<PresentationRenderer>>()
    private var isHovering = false

    fun hide() {
        inlays.apply {
            forEach { it.dispose() }
            clear()
        }
    }

    fun visualiseProgress(editor: Editor, offset: Int) {
        hide()
        val presentation = PresentationRenderer(MyInlayFactory.loadingIcon(editor))
        editor.inlayModel.addInlineElement(offset, true, presentation)?.apply {
            inlays.add(this)
        }
    }

    fun visualiseText(text: String, editor: Editor, offset: Int) {
        hide()
        createTextInlays(text, editor).forEachIndexed { index, renderer ->
            if (index == 0) {
                // if first then inline element
                editor.inlayModel.addInlineElement(offset, true, renderer)
                    ?.apply {
                        inlays.add(this)
                    }
            } else
            // block element for rest
                editor.inlayModel.addBlockElement(
                    offset,
                    true,
                    false,
                    0,
                    renderer
                )?.apply { inlays.add(this) }

        }
    }

    private fun createTextInlays(text: String, editor: Editor): List<PresentationRenderer> {

        val factory = PresentationFactory(
            (editor as EditorImpl)
        )

        if (text.isEmpty())
            return listOf()

        return text
            .split('\n')
            .map { line ->
                MyInlayFactory.prediction(line.ifEmpty { " " }, editor)
                    .let { factory.inset(it, 0, 0, 5) }
                    .let {
                        // show available actions on hover
                        factory.onHover(it, object : HoverListener {
                            override fun onHover(event: MouseEvent, translated: Point) {
                                if (isHovering)
                                    return
                                HintManager.getInstance().hideAllHints()
                                isHovering = true

                                HintManager.getInstance()
                                    .showHint(
                                        getAvailableActionsModel(editor),
                                        RelativePoint(event.locationOnScreen),
                                        HintManager.HIDE_IF_OUT_OF_EDITOR
                                                or HintManager.HIDE_BY_TEXT_CHANGE
                                                or HintManager.HIDE_BY_SCROLLING
                                                or HintManager.HIDE_BY_ANY_KEY, 0
                                    )
                            }

                            override fun onHoverFinished() {
                                HintManager.getInstance().hideAllHints()
                                isHovering = false
                            }

                        })
                    }
            }
            .map { PresentationRenderer(it) }
    }


    private fun getAvailableActionsModel(editor: Editor) = panel {

        val dataContext = DataManager.getInstance().getDataContext(editor.component)
        val manager = ActionManager.getInstance()
        val applyAction = manager.getAction("PickYourAutocompletion.ApplySuggestion")
        val previousAction = manager.getAction("PickYourAutocompletion.PreviousSuggestion")
        val nextAction = manager.getAction("PickYourAutocompletion.NextSuggestion")
        val multipleAction = manager.getAction("PickYourAutocompletion.MultipleSuggestion")

        row {
            label(" ") // ugly way for spacing, todo remove it later
            link("Apply") {
                applyAction.actionPerformed(
                    AnActionEvent.createFromDataContext(it.actionCommand, null, dataContext)
                )
            }.comment(applyAction.shortcut)
            link("Previous") {
                previousAction.actionPerformed(
                    AnActionEvent.createFromDataContext(it.actionCommand, null, dataContext)
                )
            }.comment(previousAction.shortcut)
            link("Next") {
                nextAction.actionPerformed(
                    AnActionEvent.createFromDataContext(it.actionCommand, null, dataContext)
                )
            }.comment(nextAction.shortcut)
            link("More") {
                multipleAction.actionPerformed(
                    AnActionEvent.createFromDataContext(it.actionCommand, null, dataContext)
                )
            }.comment(multipleAction.shortcut)
            label(" ") // ugly way for spacing, todo remove it later
        }.layout(RowLayout.PARENT_GRID)
    }

    private val AnAction.shortcut: String
        get() {
            this.shortcutSet.shortcuts.apply {
                if (this.isEmpty())
                    return ""
                return KeymapUtil.getShortcutText(this[0])
            }
        }
}
