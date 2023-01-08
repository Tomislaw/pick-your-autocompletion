package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton

import com.intellij.openapi.editor.Editor

class PredictionCache : ListIterator<String> {
    private var index = -1
    private var lastEditor: Editor? = null
    private var lastOffset: Int? = null

    private val cache = mutableListOf<String>()

    fun add(prediction: String) {
        index++
        cache.add(index, prediction)
    }

    fun setEditorOffset(editor: Editor?, offset: Int?) {
        if (editor != lastEditor || offset != lastOffset) {
            cache.clear()
            index = -1
        }
        lastEditor = editor
        lastOffset = offset
    }

    override fun hasNext(): Boolean = cache.size > nextIndex()

    override fun hasPrevious(): Boolean = cache.size > previousIndex() && previousIndex() >= 0

    override fun next(): String {
        if (!hasNext())
            return ""
        index = nextIndex()
        return cache[index]
    }

    override fun nextIndex(): Int = index + 1

    override fun previous(): String {
        if (!hasPrevious())
            return ""
        index = previousIndex()
        return cache[index]
    }

    override fun previousIndex(): Int = index - 1

}