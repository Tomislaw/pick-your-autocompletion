package com.github.tomislaw.pickyourautocompletion.ui.status

import com.github.tomislaw.pickyourautocompletion.errors.MissingConfigurationError
import com.github.tomislaw.pickyourautocompletion.settings.configurable.SettingsConfigurable
import com.intellij.openapi.Disposable
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.components.JBScrollPane
import com.intellij.ui.dsl.builder.panel
import com.intellij.util.ui.JBUI
import javax.swing.JComponent

@Suppress("UnstableApiUsage")
class PickYourAutocompletionStatusDialog(project: Project, private val errors: List<Throwable>) : Disposable,
    DialogWrapper(project) {

    override fun dispose() {
        super.dispose()
    }

    private val errorsListPanel = panel {
        for (error in errors)
            group {
                for (line in error.parsedMessage.split("\n"))
                    row {
                        label(line)
                    }
                if (error is MissingConfigurationError)
                    row {
                        link("Go to Configuration") {
                            ShowSettingsUtil.getInstance().showSettingsDialog(project, SettingsConfigurable::class.java)
                        }
                    }
            }
    }

    private val Throwable.parsedMessage: String get() = this.localizedMessage

    init {
        this.title = "Pick Your Autocompletion Status Feed"
        init()
    }

    override fun createCenterPanel(): JComponent = panel {
        row {
            label("There are some issues which occurred during autocompletion.")
        }
        row {
            cell(JBScrollPane(errorsListPanel)).applyToComponent {
                preferredSize = JBUI.size(600, 200)
            }
        }
    }

}