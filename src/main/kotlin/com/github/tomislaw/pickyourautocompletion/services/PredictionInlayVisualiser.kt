package com.github.tomislaw.pickyourautocompletion.services

import com.intellij.codeInsight.hint.HintManager
import com.intellij.codeInsight.hints.InlayPresentationFactory.HoverListener
import com.intellij.codeInsight.hints.presentation.PresentationFactory
import com.intellij.codeInsight.hints.presentation.PresentationRenderer
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.Inlay
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.project.ProjectManager
import com.intellij.ui.awt.RelativePoint
import java.awt.Point
import java.awt.event.MouseEvent
import javax.swing.JLabel

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

    fun visualise(text: String, editor: Editor, offset: Int) {
        hide()
        ProjectManager.getInstance().defaultProject
        predictedTextInlays(text, editor).forEachIndexed { index, renderer ->
            if (index == 0) {
                editor.inlayModel.addInlineElement(offset, true, renderer)
                    ?.apply {
                        inlays.add(this)
                    }
            } else
                editor.inlayModel.addBlockElement(
                    offset,
                    true,
                    false,
                    0,
                    renderer
                )?.apply { inlays.add(this) }

        }
    }

    private fun predictedTextInlays(predictedText: String, editor: Editor): List<PresentationRenderer> {

        val factory = PresentationFactory(
            (editor as EditorImpl)
        )

        if (predictedText.isEmpty())
            return listOf()

        return predictedText
            .split('\n')
            .map { text ->
                PredictionPresentation(editor, text.ifEmpty { " " })
                    .let { factory.inset(it, 0, 0, 5) }
                    .let {
                        factory.onHover(it, object : HoverListener {
                            override fun onHover(event: MouseEvent, translated: Point) {
                                if (isHovering)
                                    return
                                HintManager.getInstance().hideAllHints()
                                isHovering = true

                                HintManager.getInstance()
                                    .showHint(
                                        model, RelativePoint(event.locationOnScreen),
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


    private val model = JLabel("TAB - accept, CTRL + . next, CTRL + < previous, CTRL + TAB multiple predictions")
}
