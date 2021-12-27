package com.github.tomislaw.pickyourautocompletion.autocompletion.context

import com.fasterxml.jackson.core.io.JsonStringEncoder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import java.lang.StringBuilder


class MyContextBuilder : ContextBuilder {

//    val maxSize = -1
//
//    fun getContext(editor: Editor, file: VirtualFile): String {
//        val caret = editor.caretModel.offset
//        editor.caretModel.offset
//        val text = editor.document.getText(TextRange(0, caret))
//
//        StringBuilder()
//            .appendLine(file.name)
//            .appendLine("###")
//            .append(text)
//        return ""
//    }
//
//    override fun actionPerformed(e: AnActionEvent) {
//        val project = e.project
//        val editor = e.getData(CommonDataKeys.EDITOR)
//
//        val file = e.getData(CommonDataKeys.PSI_FILE)
//        val str = StringBuilder()
//        val siblings = file!!.siblings()
//        val context = file.context
//        val own = file.ownReferences
//        val refs = file.references
//        val ahh = file.children
//        for(r in  file!!.siblings()){
//            str.appendLine(r.toString())
//        }
//
//
//        Messages.showMessageDialog(e.project, str.toString(), "PSI Info", null);
//    }

    override fun create(project: Project, document: Document, offset: Int): String =
        ReadAction.compute<String, Throwable> {
            val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(document)
            val text = document.getText(TextRange(0, offset))
            return@compute StringBuilder()
                .appendLine("Language: " + psiFile?.language?.displayName)
                .appendLine(
                    "Directory: " + psiFile?.containingDirectory?.name?.replace("\\", "/")
                        ?.removePrefix("Psi") + "/" + psiFile?.name
                )
                .appendLine("###")
                .append(text)
                .let {
                    val d = it.toString()
                    val t = String(JsonStringEncoder.getInstance().quoteAsString(d))
                    println(t)
                    t
                }
        }
}
