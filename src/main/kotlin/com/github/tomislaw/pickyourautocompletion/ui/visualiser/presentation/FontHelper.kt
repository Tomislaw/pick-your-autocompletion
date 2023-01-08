package com.github.tomislaw.pickyourautocompletion.ui.visualiser.presentation

import com.intellij.ide.ui.AntialiasingType
import com.intellij.ide.ui.UISettings
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.editor.colors.TextAttributesKey
import com.intellij.openapi.editor.impl.EditorImpl
import com.intellij.openapi.editor.impl.FontInfo
import com.intellij.openapi.editor.markup.TextAttributes
import java.awt.RenderingHints
import java.awt.font.FontRenderContext

object FontHelper {
    fun getCurrentContext(editor: Editor): FontRenderContext {
        val editorContext = FontInfo.getFontRenderContext(editor.contentComponent)
        return FontRenderContext(
            editorContext.transform,
            AntialiasingType.getKeyForCurrentScope(false),
            if (editor is EditorImpl)
                UISettings.editorFractionalMetricsHint
            else
                RenderingHints.VALUE_FRACTIONALMETRICS_OFF
        )
    }
}

fun TextAttributes.withDefault(other: TextAttributes): TextAttributes {
    val result = this.clone()
    if (result.foregroundColor == null) {
        result.foregroundColor = other.foregroundColor
    }
    if (result.backgroundColor == null) {
        result.backgroundColor = other.backgroundColor
    }
    if (result.effectType == null) {
        result.effectType = other.effectType
    }
    if (result.effectColor == null) {
        result.effectColor = other.effectColor
    }
    return result
}

fun Editor.attributesOf(key: TextAttributesKey) = this.colorsScheme.getAttributes(key) ?: TextAttributes()