package com.github.tomislaw.pickyourautocompletion.listeners

import com.github.tomislaw.pickyourautocompletion.autocompletion.AutoCompletionService
import com.intellij.openapi.components.service
import com.intellij.openapi.project.Project
import com.intellij.openapi.project.ProjectManagerListener

internal class ProjectListener : ProjectManagerListener {

    override fun projectClosingBeforeSave(project: Project) {
        project.service<AutoCompletionService>().stopListening()
    }

}
