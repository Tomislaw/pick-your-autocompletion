package com.github.tomislaw.pickyourautocompletion.ui.status


import com.github.tomislaw.pickyourautocompletion.PickYourAutocompletionIcons
import com.github.tomislaw.pickyourautocompletion.listeners.AutocompletionStatusListener
import com.github.tomislaw.pickyourautocompletion.settings.SettingsState
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.project.Project
import com.intellij.openapi.util.Disposer
import com.intellij.openapi.wm.StatusBar
import com.intellij.openapi.wm.StatusBarWidget
import com.intellij.openapi.wm.StatusBarWidget.WidgetPresentation
import com.intellij.openapi.wm.StatusBarWidgetFactory
import com.intellij.util.Consumer
import java.awt.event.MouseEvent
import java.util.*
import javax.swing.Icon
import kotlin.concurrent.fixedRateTimer


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

    private val nonErrorIcon
        get() = if (SettingsState.instance.liveAutoCompletion)
            PickYourAutocompletionIcons.LogoAction
        else PickYourAutocompletionIcons.LogoActionDisabled
    private val errorIcon get() = PickYourAutocompletionIcons.LogoActionWarning

    private var icon: Icon = nonErrorIcon
    private var statusBar: StatusBar? = null
    private val errors = mutableListOf<Throwable>()
    private var flashingSignTimer: Timer? = null

    // start or enable timer used for flashing icon
    private var showingError: Boolean
        get() {
            return flashingSignTimer != null
        }
        set(value) {
            synchronized(icon) {
                when {
                    value && flashingSignTimer != null -> return
                    value && flashingSignTimer == null -> {
                        flashingSignTimer = switchIconTimer()
                    }
                    else -> {
                        flashingSignTimer?.cancel()
                        icon = nonErrorIcon
                        flashingSignTimer = null
                    }
                }
            }
        }

    // switch between ok and error icon to indicate problem
    private fun switchIconTimer() = fixedRateTimer(
        name = "flashingSignTimer",
        daemon = false,
        initialDelay = 0,
        period = 1000
    ) {
        synchronized(icon) {
            icon = if (icon != errorIcon)
                errorIcon
            else nonErrorIcon
            refresh()
        }
    }

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
                showingError = true
            }
        })
        refresh()
    }

    // show different tooltip depending on action
    override fun getTooltipText(): String = when {
        showingError -> "Show errors"
        SettingsState.instance.liveAutoCompletion -> "Disable live autocompletion"
        else -> "Enable live autocompletion"
    }

    override fun getClickConsumer(): Consumer<MouseEvent> = Consumer {
        // show errors dialog if any error occurred
        if (showingError) {
            if (PickYourAutocompletionStatusDialog(project, errors).showAndGet()) {
                errors.clear()
                showingError = false
                refresh()
            }
        } else {
            // enable or disable live autocompletion if there isn't any error
            SettingsState.instance.liveAutoCompletion = !SettingsState.instance.liveAutoCompletion
            icon = nonErrorIcon
            refresh()
        }
    }

    override fun getIcon(): Icon = icon

    override fun getPresentation(): WidgetPresentation = this

    // refresh icon
    private fun refresh() = ApplicationManager.getApplication().invokeLater {
        this.statusBar?.updateWidget(PickYourAutocompletionStatusFactory.ID)
    }
}

