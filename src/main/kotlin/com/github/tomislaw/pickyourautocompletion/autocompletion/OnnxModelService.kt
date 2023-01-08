package com.github.tomislaw.pickyourautocompletion.autocompletion

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import ai.djl.util.ClassLoaderUtils
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.github.tomislaw.pickyourautocompletion.errors.ModelFailedToLoadError
import com.github.tomislaw.pickyourautocompletion.errors.TokenizerFailedToLoadError
import com.github.tomislaw.pickyourautocompletion.settings.SettingsStateService
import com.github.tomislaw.pickyourautocompletion.settings.data.AutocompletionData
import com.intellij.openapi.Disposable
import com.intellij.openapi.components.service
import com.intellij.openapi.progress.ProgressIndicator
import com.intellij.openapi.progress.util.ProgressIndicatorBase
import com.intellij.openapi.project.ProjectManager
import com.intellij.openapi.wm.WindowManager
import com.intellij.openapi.wm.ex.ProgressIndicatorEx
import com.intellij.openapi.wm.ex.StatusBarEx
import com.intellij.openapi.wm.ex.WindowManagerEx
import kotlinx.coroutines.*
import java.nio.file.Paths
import kotlin.concurrent.thread

class OnnxModelService : Disposable {

    val tokenizer get() = runBlocking { synchronized(this) { mTokenizer } }
    val session get() = runBlocking { synchronized(this) { mSession } }
    val environment get() = runBlocking { synchronized(this) { mEnvironment } }

    private var mEnvironment: OrtEnvironment? = null
    private var mSession: OrtSession? = null
    private var mTokenizer: HuggingFaceTokenizer? = null

    private var currentModel: String? = null
    private var currentTokenizer: String? = null

    fun reload(progress: ProgressIndicator) {
        synchronized(this) {
            val state = service<SettingsStateService>().state
            if (state.autocompletionData.isConfigured
                && state.autocompletionData.builderType == AutocompletionData.BuilderType.BuiltIn
            ) {
                loadModel(
                    progress,
                    state.autocompletionData.builtInRequestBuilderData.tokenizerLocation,
                    state.autocompletionData.builtInRequestBuilderData.modelLocation
                )
            }
        }
    }

    private fun loadModel(progress: ProgressIndicator, tokenizerFile: String, modelFile: String) {
        if (tokenizerFile == currentTokenizer && modelFile == currentModel)
            return

        progress.text = "Loading model"

        mEnvironment = OrtEnvironment.getEnvironment()
        try {
            mSession?.close()
        } catch (t: Throwable) {
            println(t)
        }

        runCatching {
            thread {
                currentTokenizer = tokenizerFile
                Thread.currentThread().contextClassLoader = ClassLoaderUtils::class.java.classLoader
                mTokenizer = HuggingFaceTokenizer.newInstance(Paths.get(tokenizerFile))
            }.join()
        }
        if (mTokenizer == null) {
            throw TokenizerFailedToLoadError()
        }

        try {
            currentModel = modelFile
            mSession = environment!!.createSession(modelFile)
        } catch (t: Throwable) {
            println(t)
            throw ModelFailedToLoadError()
        }
    }

    override fun dispose() {
        runCatching {
            session?.close()
        }
    }

}