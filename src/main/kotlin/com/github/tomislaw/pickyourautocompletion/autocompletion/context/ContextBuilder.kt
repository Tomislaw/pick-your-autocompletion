package com.github.tomislaw.pickyourautocompletion.autocompletion.context

import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project

interface ContextBuilder {
    fun create(project: Project, editor: Editor, offset: Int): String
}