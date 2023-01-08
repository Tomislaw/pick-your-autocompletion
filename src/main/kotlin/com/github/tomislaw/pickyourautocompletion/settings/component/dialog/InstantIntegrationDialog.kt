package com.github.tomislaw.pickyourautocompletion.settings.component.dialog

import com.github.tomislaw.pickyourautocompletion.settings.SettingsStateService
import com.github.tomislaw.pickyourautocompletion.settings.data.AutocompletionData
import com.github.tomislaw.pickyourautocompletion.settings.data.PromptBuilderData
import com.github.tomislaw.pickyourautocompletion.settings.data.WebRequestBuilderData
import com.intellij.codeInsight.hint.HintUtil
import com.intellij.openapi.components.service
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.components.JBPasswordField
import com.intellij.ui.dsl.builder.bindText
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import java.time.Duration
import javax.swing.JComponent

@Suppress("UnstableApiUsage")
class InstantIntegrationDialog(title: String) : DialogWrapper(true) {

    var apiKey: String = ""

    init {
        this.title = title
        init()
    }

    override fun createCenterPanel(): JComponent = panel {
        row("Api Key") {
            cell(JBPasswordField()).horizontalAlign(HorizontalAlign.FILL).bindText({ apiKey }, { apiKey = it })
        }
    }

    companion object {
        fun addOpenAiIntegration(component: JComponent, isSuccess: (Boolean) -> Unit = {}) {
            InstantIntegrationDialog("OpenAi Integration").apply {
                if (!showAndGet())
                    return

                // validate OpenAi api key
                val response = OkHttpClient()
                    .newCall(
                        WebRequestBuilderData.validatorFromProperties("openai.validate", apiKey)
                    ).execute()

                isSuccess(response.isSuccessful)

                if (!response.isSuccessful)
                    return showWarning(
                        "Failed to authenticate with OpenAi.",
                        response.body?.string() ?: "",
                        component
                    )
                showSuccessful(
                    "Successfully authenticated with OpenAi",
                    "",
                    component
                )

                val state = service<SettingsStateService>().state.autocompletionData
                state.webRequestBuilderData = WebRequestBuilderData.fromProperties("openai", apiKey)
                state.promptBuilderData = PromptBuilderData.fromProperties("default")
                state.builderType = AutocompletionData.BuilderType.Web
                reloadData()
            }
        }

        fun addHuggingFaceIntegration(component: JComponent, isSuccess: (Boolean) -> Unit = {}) {
            InstantIntegrationDialog("Hugging Face Integration").apply {
                if (!showAndGet())
                    return

                // validate OpenAi api key
                val response = OkHttpClient()
                    .newBuilder()
                    .connectTimeout(Duration.ofSeconds(10))
                    .build()
                    .newCall(
                        WebRequestBuilderData.validatorFromProperties("huggingface.validato", apiKey)
                    )
                    .execute()

                isSuccess(response.isSuccessful)

                if (!response.isSuccessful)
                    return showWarning(
                        "Failed to authenticate with Hugging Face.",
                        "",
                        component
                    )

                showSuccessful(
                    "Successfully authenticated with Hugging Face",
                    "",
                    component
                )

                // create ApiKey
                val state = service<SettingsStateService>().state.autocompletionData
                state.webRequestBuilderData = WebRequestBuilderData.fromProperties("huggingface", apiKey)
                state.promptBuilderData = PromptBuilderData.fromProperties("default")
                state.builderType = AutocompletionData.BuilderType.Web
                reloadData()
            }
        }

        private fun showWarning(title: String, description: String, component: JComponent) {
            JBPopupFactory.getInstance().createBalloonBuilder(
                panel {
                    row {
                        text(title, 50)
                    }
                    row {
                        text(description, 50)
                    }
                }.apply {
                    background = HintUtil.getErrorColor()
                }
            )
                .setFadeoutTime(6000)
                .setFillColor(HintUtil.getErrorColor())
                .createBalloon()
                .show(RelativePoint.getNorthEastOf(component), Balloon.Position.atRight)
        }

        private fun showSuccessful(title: String, description: String, component: JComponent) {
            JBPopupFactory.getInstance().createBalloonBuilder(
                panel {
                    row {
                        text(title, 50)
                    }
                    row {
                        text(description, 50)
                    }
                }.apply {
                    background = HintUtil.getQuestionColor()
                }
            )
                .setFadeoutTime(1000)
                .setFillColor(HintUtil.getQuestionColor())
                .createBalloon()
                .show(RelativePoint.getNorthEastOf(component), Balloon.Position.atRight)
        }

        private fun reloadData() {
            service<SettingsStateService>().settingsChanged()
        }
    }
}