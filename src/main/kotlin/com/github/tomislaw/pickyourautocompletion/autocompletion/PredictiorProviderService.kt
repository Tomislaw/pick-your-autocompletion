package com.github.tomislaw.pickyourautocompletion.autocompletion

import com.github.tomislaw.pickyourautocompletion.autocompletion.context.ContextBuilder
import com.github.tomislaw.pickyourautocompletion.autocompletion.context.MultiFileContextBuilder
import com.github.tomislaw.pickyourautocompletion.autocompletion.predictor.Predictor
import com.github.tomislaw.pickyourautocompletion.autocompletion.predictor.SmartStopProvider
import com.github.tomislaw.pickyourautocompletion.autocompletion.predictor.webhook.WebhookPredictor
import com.github.tomislaw.pickyourautocompletion.settings.SettingsState
import com.github.tomislaw.pickyourautocompletion.settings.data.integrations.WebhookIntegration
import com.intellij.openapi.application.ApplicationManager
import com.intellij.openapi.editor.Editor
import com.intellij.openapi.project.Project


class PredictiorProviderService {

    private val predictors = mutableListOf<Predictor>()
    private val contextBuilders = mutableListOf<ContextBuilder>()

    init {
        reload()
    }

    fun reload() {
        predictors.clear()
        contextBuilders.clear()
        contextBuilders.add(MultiFileContextBuilder())
        SettingsState.instance.entryPoints.forEach {
            when (it) {
                is WebhookIntegration -> predictors.add(WebhookPredictor(it))
            }
        }
    }

    fun predict(project: Project, editor: Editor, offset: Int): String {
        val stop = SmartStopProvider.getStopString(offset, editor)
        val context = contextBuilders.first().create(project, editor, offset)
        return predictors.first().predict(context, 1024, stop)
    }


    companion object {
        val instance: PredictiorProviderService
            get() = ApplicationManager.getApplication().getService(PredictiorProviderService::class.java)
    }
}
