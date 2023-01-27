package com.github.tomislaw.pickyourautocompletion.settings.component.dialog

import ai.onnxruntime.OrtEnvironment
import com.github.tomislaw.pickyourautocompletion.errors.ModelFailedToDownload
import com.github.tomislaw.pickyourautocompletion.listeners.AutocompletionStatusListener
import com.github.tomislaw.pickyourautocompletion.localizedText
import com.github.tomislaw.pickyourautocompletion.settings.SettingsStateService
import com.github.tomislaw.pickyourautocompletion.settings.data.AutocompletionData
import com.github.tomislaw.pickyourautocompletion.settings.data.BuiltInRequestBuilderData
import com.github.tomislaw.pickyourautocompletion.settings.data.PredictionSanitizerData
import com.github.tomislaw.pickyourautocompletion.settings.data.PromptBuilderData
import com.github.tomislaw.pickyourautocompletion.utils.DownloadUtils
import com.intellij.openapi.application.PathManager
import com.intellij.openapi.components.service
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.progress.TaskInfo
import com.intellij.openapi.progress.util.ProgressIndicatorBase
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.openapi.wm.ex.ProgressIndicatorEx
import com.intellij.openapi.wm.ex.StatusBarEx
import com.intellij.openapi.wm.ex.WindowManagerEx
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.util.io.createFile
import com.intellij.util.io.delete
import com.intellij.util.io.exists
import kotlinx.coroutines.*
import java.nio.file.Path
import java.nio.file.Paths
import javax.swing.JComponent
import kotlin.io.path.pathString


class BuiltInIntegrationDialog : DialogWrapper(true) {

    private val downloadUrl = JBTextField().apply {
        text = BuiltInRequestBuilderData.properties.property("default", "url") ?: ""
    }
    private val downloadFolder = TextFieldWithBrowseButton().apply {
        text = DOWNLOAD_BASE_PATH.toString()
        addBrowseFolderListener(
            TextBrowseFolderListener(
                FileChooserDescriptor(
                    true, true, false, false, true, false
                ).withShowHiddenFiles(false)
            )
        )

    }
    private val devices = ComboBox(ortDevices)

    private val ortDevices get() = OrtEnvironment.getAvailableProviders().toTypedArray()

    init {
        this.title = "Built-in Integration"
        init()
    }

    private fun downloadModelFiles(progress: ProgressIndicatorBase) = CoroutineScope(Dispatchers.IO).async {
        progress.text = "Downloading tokenizer"
        val tokenizerPath = Paths.get(downloadFolder.text).resolve(TOKENIZER_FILE)
        val tokenizerUrl = downloadUrl.text + TOKENIZER_FILE

        val tokenizerDownloadResult = downloadModelFile(tokenizerPath, tokenizerUrl, progress)
        if (tokenizerDownloadResult.isFailure)
            return@async tokenizerDownloadResult

        progress.text = "Downloading model"
        val modelPath = Paths.get(downloadFolder.text).resolve(MODEL_FILE)
        val modelUrl = downloadUrl.text + MODEL_FILE
        val modelDownloadResult = downloadModelFile(modelPath, modelUrl, progress)
        if (modelDownloadResult.isFailure)
            return@async modelDownloadResult

        val state = service<SettingsStateService>().state.autocompletionData
        state.builtInRequestBuilderData = BuiltInRequestBuilderData.fromProperties(
            builder = "default",
            model = modelPath.pathString,
            tokenizer = tokenizerPath.pathString,
            device = devices.item.ordinal
        )
        state.promptBuilderData = PromptBuilderData.fromProperties("default")
        state.builderType = AutocompletionData.BuilderType.BuiltIn
        state.predictionSanitizerData = PredictionSanitizerData.fromProperties("default")
        return@async Result.success(Unit)
    }

    private suspend fun downloadModelFile(path: Path, url: String, indicator: ProgressIndicatorBase): Result<Unit> {

        if (!path.exists()) {
            path.createFile()
        } else {
            path.delete()
            path.createFile()
        }
        return DownloadUtils.downloadFile(url, path.toFile(), indicator)
    }

    override fun showAndGet(): Boolean {
        val ok = super.showAndGet()
        if (ok) {
            val progress = ProgressIndicatorBase()
            addToStatusBar(progress)
            CoroutineScope(Dispatchers.Default).launch {
                runCatching { downloadModelFiles(progress).await().getOrThrow() }
                    .onFailure { th ->
                        ProjectManager.getInstance().openProjects.forEach {
                            it.messageBus.syncPublisher(AutocompletionStatusListener.TOPIC)
                                .onError(ModelFailedToDownload(th))
                        }
                    }.onSuccess { reloadData() }
                removeFromStatusBar(progress)
            }
        }
        return ok
    }

    private val downloadModelInfo = object : TaskInfo {
        override fun getTitle(): String = "Downloading model"
        override fun getCancelText(): String = ""
        override fun getCancelTooltipText(): String = ""
        override fun isCancellable(): Boolean = true

    }

    private fun addToStatusBar(progress: ProgressIndicatorEx) {
        val frame = WindowManagerEx.getInstanceEx().findFrameFor(null) ?: return
        val statusBar = frame.statusBar as? StatusBarEx ?: return
        statusBar.addProgress(progress, downloadModelInfo)
    }

    private fun removeFromStatusBar(progress: ProgressIndicatorEx) {
        progress.finish(downloadModelInfo)
    }

    override fun createCenterPanel(): JComponent = panel {
        group("Help") {
            row {
                comment(localizedText("dialog.model"))
            }
        }
        group(title = "Model Url") {
            row {
                cell(downloadUrl).horizontalAlign(HorizontalAlign.FILL)
            }
        }
        group(title = "Target Folder") {
            row {
                cell(downloadFolder).horizontalAlign(HorizontalAlign.FILL)
            }
        }
        group("Device") {
            row {
                cell(devices).horizontalAlign(HorizontalAlign.FILL)
            }
        }
    }

    companion object {
        private val DOWNLOAD_BASE_PATH =
            Paths.get(PathManager.getAbsolutePath("~/"))
                .resolve("pick-your-autocompletion")
                .resolve("models")
                .resolve("codegen-350m-multi-onnx")
        private val MODEL_FILE = "model.onnx"
        private val TOKENIZER_FILE = "tokenizer.json"

        private fun reloadData() {
            service<SettingsStateService>().settingsChanged()
        }
    }
}