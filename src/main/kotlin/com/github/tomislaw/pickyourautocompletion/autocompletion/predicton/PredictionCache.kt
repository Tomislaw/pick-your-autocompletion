package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton

import com.intellij.openapi.editor.Editor

class PredictionCache : ListIterator<String> {
    private var index = -1
    private var lastEditor: Editor? = null
    private var lastOffset: Int? = null

    private val cache = mutableListOf<String>()

    @Synchronized
    fun add(prediction: String) {
        index++
        cache.add(index, prediction)
    }

    @Synchronized
    fun setEditorOffset(editor: Editor?, offset: Int?) {
        if (editor != lastEditor || offset != lastOffset) {
            cache.clear()
            index = -1
        }
        lastEditor = editor
        lastOffset = offset
    }

    @Synchronized
    override fun hasNext(): Boolean = cache.size > nextIndex()

    @Synchronized
    override fun hasPrevious(): Boolean = cache.size > previousIndex() && previousIndex() >= 0

    @Synchronized
    override fun next(): String {
        if (!hasNext())
            return ""
        index = nextIndex()
        return cache[index]
    }

    @Synchronized
    override fun nextIndex(): Int = index + 1

    @Synchronized
    override fun previous(): String {
        if (!hasPrevious())
            return ""
        index = previousIndex()
        return cache[index]
    }

    @Synchronized
    override fun previousIndex(): Int = index - 1

}