// Copyright 2000-2019 JetBrains s.r.o. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.tomislaw.pickyourautocompletion.ui.visualiser.presentation

import com.intellij.codeInsight.hints.presentation.BasePresentation
import com.intellij.ide.ui.AntialiasingType
import com.intellij.openapi.editor.DefaultLanguageHighlighterColors
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.EditorFontType
import com.intellij.openapi.editor.impl.FontInfo
import com.intellij.openapi.editor.markup.EffectType
import com.intellij.openapi.editor.markup.TextAttributes
import com.intellij.ui.paint.EffectPainter
import java.awt.FontMetrics
import java.awt.Graphics2D
import java.awt.RenderingHints
import kotlin.math.ceil

/**
 * Didn't find any class where I can change font size or font, so here it is
 */
open class PredictionPresentation(
    private val editor: Editor,
    var text: String
) : BasePresentation() {

    override val width: Int
        get() = metrics.stringWidth(text)

    override val height: Int

    private val fontBaseline: Int
    private val metrics: FontMetrics

    init {
        val font = editor.colorsScheme.getFont(EditorFontType.CONSOLE_PLAIN)
        val context = FontHelper.getCurrentContext(editor)
        metrics = FontInfo.getFontMetrics(font, context)

        fontBaseline = ceil(metrics.font.createGlyphVector(context, "Alb").visualBounds.height).toInt()
        height = ceil(metrics.font.createGlyphVector(context, "Albpq@").visualBounds.height).toInt()
    }

    override fun paint(g: Graphics2D, attributes: TextAttributes) {

        val textAttributes =
            attributes.withDefault(editor.attributesOf(DefaultLanguageHighlighterColors.NUMBER))
        val savedHint = g.getRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING)

        try {
            val foreground = textAttributes.foregroundColor
            if (foreground != null) {
                val font = metrics.font
                g.font = font
                g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, AntialiasingType.getKeyForCurrentScope(false))
                g.color = foreground
                g.drawString(text, 0, fontBaseline)
                val effectColor = textAttributes.effectColor
                if (effectColor != null) {
                    g.color = effectColor
                    when (textAttributes.effectType) {
                        EffectType.LINE_UNDERSCORE -> EffectPainter.LINE_UNDERSCORE
                            .paint(g, 0, metrics.ascent, width, metrics.descent, font)
                        EffectType.BOLD_LINE_UNDERSCORE -> EffectPainter.BOLD_LINE_UNDERSCORE
                            .paint(g, 0, metrics.ascent, width, metrics.descent, font)
                        EffectType.STRIKEOUT -> EffectPainter.STRIKE_THROUGH
                            .paint(g, 0, metrics.ascent, width, height, font)
                        EffectType.WAVE_UNDERSCORE -> EffectPainter.WAVE_UNDERSCORE
                            .paint(g, 0, metrics.ascent, width, metrics.descent, font)
                        EffectType.BOLD_DOTTED_LINE -> EffectPainter.BOLD_DOTTED_UNDERSCORE
                            .paint(g, 0, metrics.ascent, width, metrics.descent, font)
                        else -> {}
                    }
                }
            }
        } finally {
            g.setRenderingHint(RenderingHints.KEY_TEXT_ANTIALIASING, savedHint)
        }
    }

    override fun toString(): String = text
}



