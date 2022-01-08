package com.github.tomislaw.pickyourautocompletion.status


import com.github.tomislaw.pickyourautocompletion.PickYourAutocompletionIcons
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.vcs.changes.ui.ChangesViewContentManager
import com.intellij.openapi.vcs.changes.ui.ChangesViewContentManager.Companion.INCOMING
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidget.WidgetPresentation
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.openapi.wm.ToolWindowManager
import com.intellij.util.Consumer
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

    private var statusBar: StatusBar? = null
    override fun dispose() {
        statusBar = null
    }

    override fun ID(): String = PickYourAutocompletionStatusFactory.ID

    override fun install(statusBar: StatusBar) {
        this.statusBar = statusBar

//        val busConnection = project.messageBus.connect(this)
//        busConnection.subscribe(COMMITTED_TOPIC, object : CommittedChangesListener {
//            override fun incomingChangesUpdated(receivedChanges: List<CommittedChangeList>?) = refresh()
//            override fun changesCleared() = refresh()
//        })
//        busConnection.subscribe(VCS_CONFIGURATION_CHANGED, VcsListener { refresh() })
//        busConnection.subscribe(VCS_CONFIGURATION_CHANGED_IN_PLUGIN, VcsListener { refresh() })
//        refresh()
    }

    override fun getTooltipText(): String = "Ahh"

    override fun getClickConsumer(): Consumer<MouseEvent> =
        Consumer {
            val toolWindow =
                ToolWindowManager.getInstance(project).getToolWindow(ChangesViewContentManager.TOOLWINDOW_ID)
            toolWindow?.show { ChangesViewContentManager.getInstance(project).selectContent(INCOMING) }
        }

    override fun getIcon(): Icon = PickYourAutocompletionIcons.LogoAction

    override fun getPresentation(): WidgetPresentation = this

//    private fun refresh() =
//        runInEdt {
//            if (project.isDisposed || statusBar == null) return@runInEdt
//
//            isIncomingChangesAvailable = IncomingChangesViewProvider.VisibilityPredicate().test(project)
//            incomingChangesCount = if (isIncomingChangesAvailable) getCachedIncomingChangesCount() else 0
//        }

}

