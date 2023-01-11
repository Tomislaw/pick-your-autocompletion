package com.github.tomislaw.pickyourautocompletion.ui.status

import com.github.tomislaw.pickyourautocompletion.Icons
import com.github.tomislaw.pickyourautocompletion.listeners.AutocompletionStatusListener
import com.github.tomislaw.pickyourautocompletion.localizedText
import com.github.tomislaw.pickyourautocompletion.settings.SettingsStateService
import com.intellij.icons.AllIcons
import com.intellij.ide.DataManager
import com.intellij.openapi.actionSystem.*
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.service
import com.intellij.openapi.project.DumbAwareAction
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidget.WidgetPresentation
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.util.Consumer
import com.intellij.vcs.commit.NonModalCommitPanel.Companion.showAbove
import java.awt.event.MouseEvent
import javax.swing.Icon


class PickYourAutocompletionStatusFactory : StatusBarWidgetFactory {
    companion object {
        const val ID = "PickYourAutocompletionStatus"
    }

    override fun getId(): String = ID
    override fun getDisplayName(): String = "Pick Your Autocompletion"
    override fun isAvailable(project: Project): Boolean = true
    override fun createWidget(project: Project): StatusBarWidget = PickYourAutocompletionStatus(project)
    override fun disposeWidget(widget: StatusBarWidget) = Disposer.dispose(widget)
    override fun canBeEnabledOn(statusBar: StatusBar): Boolean = true
}

private class PickYourAutocompletionStatus(private val project: Project) : StatusBarWidget,
    StatusBarWidget.IconPresentation {

    private val normalIcon
        get() = if (service<SettingsStateService>().state.liveAutoCompletionEnabled)
            Icons.LogoAction
        else Icons.LogoActionDisabled
    private val errorIcon
        get() = if (service<SettingsStateService>().state.liveAutoCompletionEnabled)
            Icons.WarningEnabled
        else Icons.WarningDisabled

    private var statusBar: StatusBar? = null
    private val errors = mutableListOf<Throwable>()

    override fun dispose() {
        statusBar = null
    }

    override fun ID(): String = PickYourAutocompletionStatusFactory.ID

    override fun install(statusBar: StatusBar) {
        this.statusBar = statusBar

        // listen for errors
        val busConnection = project.messageBus.connect(this)
        busConnection.subscribe(AutocompletionStatusListener.TOPIC, object : AutocompletionStatusListener {
            override fun onError(throwable: Throwable) {
                errors.add(throwable)
                refresh()
            }

            override fun onLiveAutocompletionChanged() {
                refresh()
            }
        })
        refresh()
    }

    // show different tooltip depending on action
    override fun getTooltipText(): String = when {
        errors.size > 0 -> "Show errors"
        service<SettingsStateService>().state.liveAutoCompletionEnabled -> "Disable live autocompletion"
        else -> "Enable live autocompletion"
    }

    private fun createActionGroup(): ActionGroup {

        val icon = if (errors.size > 0) AllIcons.General.Error else null
        val manager = ActionManager.getInstance()
        return DefaultActionGroup(
            manager.getAction("PickYourAutocompletion.ToggleLiveAutoCompletion"),
            ErrorLogsAction(icon, "Error Logs (${errors.size})"),
            manager.getAction("PickYourAutocompletion.ShowOptions"),
        )
    }

    override fun getClickConsumer(): Consumer<MouseEvent> = Consumer {
        val dataContext = DataManager.getInstance().getDataContext(it.component)
        JBPopupFactory.getInstance().createActionGroupPopup(
            localizedText("plugin.name"), createActionGroup(), dataContext,
            JBPopupFactory.ActionSelectionAid.SPEEDSEARCH, true
        ).showInBestPositionFor(dataContext)
    }

    override fun getIcon(): Icon = if (errors.size > 0) errorIcon else normalIcon

    override fun getPresentation(): WidgetPresentation = this

    // refresh icon
    private fun refresh() = ApplicationManager.getApplication().invokeLater {
        this.statusBar?.updateWidget(PickYourAutocompletionStatusFactory.ID)
    }

    inner class ErrorLogsAction(icon: Icon?, message: String) : DumbAwareAction(message, "", icon) {
        override fun actionPerformed(e: AnActionEvent) {
            val ok = PickYourAutocompletionStatusDialog(project, errors).showAndGet()
            if (ok) {
                errors.clear()
                refresh()
            }
        }
    }
}

