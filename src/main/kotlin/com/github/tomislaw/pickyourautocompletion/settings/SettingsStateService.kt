// Copyright 2000-2021 JetBrains s.r.o. and other contributors. Use of this source code is governed by the Apache 2.0 license that can be found in the LICENSE file.
package com.github.tomislaw.pickyourautocompletion.settings

import com.github.tomislaw.pickyourautocompletion.autocompletion.OnnxModelService
import com.github.tomislaw.pickyourautocompletion.autocompletion.PredictorProviderService
import com.github.tomislaw.pickyourautocompletion.listeners.AutocompletionStatusListener
import com.github.tomislaw.pickyourautocompletion.settings.configurable.PromptBuildersConfigurable
import com.github.tomislaw.pickyourautocompletion.settings.configurable.RequestBuilderConfigurable
import com.github.tomislaw.pickyourautocompletion.settings.configurable.SettingsConfigurable
import com.github.tomislaw.pickyourautocompletion.settings.data.AutocompletionData
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.components.PersistentStateComponent
import com.intellij.openapi.components.State
import com.intellij.openapi.components.Storage
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.TaskInfo
import com.intellij.openapi.progress.util.ProgressIndicatorBase
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.ex.ProgressIndicatorEx
import com.intellij.openapi.wm.ex.StatusBarEx
import com.intellij.openapi.wm.ex.WindowManagerEx
import com.intellij.util.xmlb.XmlSerializerUtil
import kotlinx.coroutines.*

/**
 * Supports storing the application settings in a persistent way.
 * The [State] and [Storage] annotations define the name of the data and the file name where
 * these persistent application settings are stored.
 */
@State(
    name = "com.github.tomislaw.pickyourautocompletion.SettingsState",
    storages = [Storage("PickYourAutocompletion.xml")]
)
class SettingsStateService : PersistentStateComponent<SettingsStateService.State> {

    private var state = State()
    private val scope = CoroutineScope(Dispatchers.Default)
    private var job: Job? = null

    override fun getState(): State = state

    override fun loadState(state: State) {
        XmlSerializerUtil.copyBean(state, this.state)
    }

    data class State(
        var autocompletionData: AutocompletionData = AutocompletionData(),
        var liveAutoCompletionEnabled: Boolean = false,
        var firstUse: Boolean = true,
    )

    private var refreshProgress = ProgressIndicatorBase()

    @Synchronized
    fun settingsChanged() {
        runBlocking {
            withContext(Dispatchers.IO) {
                removeFromStatusBar(refreshProgress)
                refreshProgress = ProgressIndicatorBase()
                refreshProgress.isIndeterminate = true
                addToStatusBar(refreshProgress)
            }
        }

        job?.cancel()

        val handler = CoroutineExceptionHandler { _, exception ->
            ProjectManager.getInstance().openProjects.forEach {
                it.messageBus.syncPublisher(AutocompletionStatusListener.TOPIC).onError(exception)
                removeFromStatusBar(refreshProgress)
            }
        }

        job = scope.launch(handler) {
            RequestBuilderConfigurable.instance?.reset()
            PromptBuildersConfigurable.instance?.reset()
            SettingsConfigurable.instance?.reset()
            service<OnnxModelService>().reload(refreshProgress)

            refreshProgress.text = "Loading Autocompletion Builder"
            ProjectManager.getInstance().openProjects.forEach {
                it.service<PredictorProviderService>().reload()
                it.messageBus.syncPublisher(AutocompletionStatusListener.TOPIC).onLiveAutocompletionChanged()
            }
        }.apply {
            invokeOnCompletion {
                removeFromStatusBar(refreshProgress)
            }
        }
    }

    val refreshTaskInfo = object : TaskInfo {
        override fun getTitle(): String = "Reloading Pick Your Autocompletion config"
        override fun getCancelText(): String = ""
        override fun getCancelTooltipText(): String = ""
        override fun isCancellable(): Boolean = false

    }

    private fun addToStatusBar(progress: ProgressIndicatorEx) = ApplicationManager.getApplication().invokeLater {
        val frame = WindowManagerEx.getInstanceEx().findFrameFor(null) ?: return@invokeLater
        val statusBar = frame.statusBar as? StatusBarEx ?: return@invokeLater
        statusBar.addProgress(progress, refreshTaskInfo)
    }


    private fun removeFromStatusBar(progress: ProgressIndicatorEx) {
        progress.finish(refreshTaskInfo)
    }

}