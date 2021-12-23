package com.github.tomislaw.pickyourautocompletion.autocompletion.context

import com.intellij.openapi.actionSystem.AnAction
import com.intellij.openapi.actionSystem.AnActionEvent
import com.intellij.openapi.actionSystem.CommonDataKeys
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.ui.Messages
import com.intellij.openapi.util.TextRange
import com.intellij.openapi.vfs.VirtualFile
import com.intellij.psi.util.siblings

class ContextBuilder : AnAction() {

    val maxSize = -1

    fun getContext(editor: Editor, file: VirtualFile): String {
        val caret = editor.caretModel.offset
        editor.caretModel.offset
        val text = editor.document.getText(TextRange(0, caret))

        StringBuilder()
            .appendLine(file.name)
            .appendLine("###")
            .append(text)
        return ""
    }

    override fun actionPerformed(e: AnActionEvent) {
        val project = e.project
        val editor = e.getData(CommonDataKeys.EDITOR)

        val file = e.getData(CommonDataKeys.PSI_FILE)
        val str = StringBuilder()
        val siblings = file!!.siblings()
        val context = file.context
        val own = file.ownReferences
        val refs = file.references
        val ahh = file.children
        for(r in  file!!.siblings()){
            str.appendLine(r.toString())
        }


        Messages.showMessageDialog(e.project, str.toString(), "PSI Info", null);
    }

}