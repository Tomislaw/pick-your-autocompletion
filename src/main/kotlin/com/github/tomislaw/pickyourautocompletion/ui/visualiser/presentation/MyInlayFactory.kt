@file:Suppress("UnstableApiUsage")

package com.github.tomislaw.pickyourautocompletion.ui.visualiser.presentation

import com.github.tomislaw.pickyourautocompletion.Icons
import com.intellij.codeInsight.hints.presentation.InlayPresentation
import com.intellij.codeInsight.hints.presentation.InsetPresentation
import com.intellij.codeInsight.hints.presentation.ScaleAwareIconPresentation
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.editor.impl.FontInfo
import kotlin.math.ceil

object MyInlayFactory {
    fun loadingIcon(editor: Editor): InlayPresentation {
        val font = editor.colorsScheme.getFont(EditorFontType.CONSOLE_PLAIN)
        val context = FontHelper.getCurrentContext(editor)
        val metrics = FontInfo.getFontMetrics(font, context)
        val height = ceil(metrics.font.createGlyphVector(context, "Albpq@").visualBounds.height).toInt()

        return InsetPresentation(
            ScaleAwareIconPresentation(Icons.LoadingPrediction, editor, "Loading", 1),
            1, 0, height / 2
        )
    }

    fun prediction(text: String, editor: Editor): InlayPresentation = PredictionPresentation(editor, text)

}