package com.github.tomislaw.pickyourautocompletion.window

import com.intellij.openapi.project.Project
import com.intellij.openapi.wm.ToolWindow
import com.intellij.openapi.wm.ToolWindowFactory
import com.intellij.ui.content.ContentFactory

//class SettingsWindowFactory : ToolWindowFactory {
//    override fun createToolWindowContent(project: Project, toolWindow: ToolWindow) {
//        val window = SettingsWindow()
//
//        ContentFactory.SERVICE.getInstance()
//            .createContent(window.component,"",false)
//            .apply {
//                toolWindow.contentManager.addContent(this)
//            }
//    }
//}