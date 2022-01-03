package com.github.tomislaw.pickyourautocompletion.autocompletion.context

import com.fasterxml.jackson.core.io.JsonStringEncoder
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.application.ReadAction
import com.intellij.openapi.editor.Document
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.TextRange
import com.intellij.psi.PsiDocumentManager
import com.intellij.psi.PsiElement
import com.intellij.psi.SyntaxTraverser
import com.intellij.psi.search.LocalSearchScope
import com.intellij.psi.search.SearchScope
import com.intellij.psi.search.searches.ReferencesSearch
import com.intellij.psi.util.elementType
import com.intellij.psi.util.elementsAroundOffsetUp
import com.intellij.ui.tree.AbstractTreeWalker
import java.lang.StringBuilder


class MyContextBuilder : ContextBuilder {


    override fun create(project: Project, editor: Editor, offset: Int): String =
        ReadAction.compute<String, Throwable> {

            val psiFile = PsiDocumentManager.getInstance(project).getPsiFile(editor.document)

//            var element = psiFile?.findElementAt(offset - 2)
//            for (count in 0..10){
//                println(element?.navigationElement?.containingFile)
//                element = element?.prevSibling
//            }

//            val ahhs = SyntaxTraverser
//                .psiTraverser(element?.parent)
//                .traverse()
//                .transform(PsiElement::getReferences)
//                .filter { it.isNotEmpty() }
//                .toList().forEach {
//                    it.forEach {
//                        println(it.element.navigationElement.ownReferences)
//                    }
//                }
//            val ahh = element?.node?.psi?.reference?.resolve()?.containingFile
            val text = editor.document.getText(TextRange(0, offset))
            return@compute StringBuilder()
                .appendLine("Language: " + psiFile?.language?.displayName)
                .appendLine(
                    "Directory: " + psiFile?.containingDirectory?.name?.replace("\\", "/")
                        ?.removePrefix("Psi") + "/" + psiFile?.name
                )
                .appendLine("###")
                .append(text)
                .toString()
        }
}
