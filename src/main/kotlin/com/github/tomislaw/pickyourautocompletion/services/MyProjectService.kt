package com.github.tomislaw.pickyourautocompletion.services

import com.intellij.openapi.project.Project
import com.github.tomislaw.pickyourautocompletion.MyBundle

class MyProjectService(project: Project) {

    init {
        println(MyBundle.message("projectService", project.name))
    }
}
