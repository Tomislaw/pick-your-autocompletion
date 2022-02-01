package com.github.tomislaw.pickyourautocompletion.ui.status

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import javax.swing.JComponent

@Suppress("UnstableApiUsage")
class PickYourAutocompletionDialog(project: Project, private val errors: List<Throwable>) :
    DialogWrapper(project) {

    private val errorsListPanel = panel {
        for (element in errors) {
            row {
                cell(JBTextArea(element.parsedMessage)).applyToComponent {
                    preferredSize = JBUI.size(590, 50)
                }
            }
        }
    }

    init {
        this.title = "Errors Occurred During Autocompletion"
        init()
    }

    private val Throwable.parsedMessage: String get() = this.localizedMessage


    override fun createCenterPanel(): JComponent = panel {
        row {
            label("There were some errors which occurred during autocompletion.")
        }
        row {
            cell(JBScrollPane(errorsListPanel)).applyToComponent {
                preferredSize = JBUI.size(600, 200)
            }
        }
    }


}