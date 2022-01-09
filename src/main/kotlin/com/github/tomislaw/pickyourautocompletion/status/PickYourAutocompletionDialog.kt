package com.github.tomislaw.pickyourautocompletion.status

import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.layout.applyToComponent
import com.intellij.ui.layout.panel
import com.intellij.util.ui.JBUI
import javax.swing.JComponent

@Suppress("UnstableApiUsage")
class PickYourAutocompletionDialog(project: Project, private val errors: List<Throwable>) :
    DialogWrapper(project) {

    private val errorsListPanel = panel {
        for (element in errors) {
            row {
                component(JBTextArea(element.parsedMessage)).applyToComponent {
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
            scrollPane(errorsListPanel).applyToComponent {
                preferredSize = JBUI.size(600, 200)
            }
        }
    }


}