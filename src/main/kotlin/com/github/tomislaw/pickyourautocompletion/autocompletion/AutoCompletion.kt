package com.github.tomislaw.pickyourautocompletion.autocompletion

import com.intellij.openapi.Disposable
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import javax.swing.event.CaretListener
import javax.swing.event.DocumentListener

interface AutoCompletion : Disposable, DocumentListener, CaretListener {

}