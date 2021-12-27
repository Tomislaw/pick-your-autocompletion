package com.github.tomislaw.pickyourautocompletion.autocompletion.context

import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project

interface ContextBuilder {
    fun create(project: Project, document: Document, offset: Int): String
}