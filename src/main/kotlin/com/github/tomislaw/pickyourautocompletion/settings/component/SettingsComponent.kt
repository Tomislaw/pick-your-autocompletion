package com.github.tomislaw.pickyourautocompletion.settings.component

import com.github.tomislaw.pickyourautocompletion.PickYourAutocompletionIcons
import com.github.tomislaw.pickyourautocompletion.autocompletion.PredictorProviderService
import com.github.tomislaw.pickyourautocompletion.settings.SettingsState
import com.github.tomislaw.pickyourautocompletion.settings.component.dialog.InstantIntegrationDialog
import com.github.tomislaw.pickyourautocompletion.settings.configurable.EntryPointsConfigurable
import com.github.tomislaw.pickyourautocompletion.settings.configurable.PasswordsConfigurable
import com.github.tomislaw.pickyourautocompletion.settings.configurable.PromptBuildersConfigurable
import com.github.tomislaw.pickyourautocompletion.settings.data.ApiKey
import com.github.tomislaw.pickyourautocompletion.settings.data.integrations.WebhookIntegration
import com.intellij.codeInsight.hint.HintUtil
import com.intellij.openapi.ui.DialogPanel
import com.intellij.openapi.ui.popup.Balloon
import com.intellij.openapi.ui.popup.JBPopupFactory
import com.intellij.ui.awt.RelativePoint
import com.intellij.ui.dsl.builder.DEFAULT_COMMENT_WIDTH
import com.intellij.ui.dsl.builder.RowLayout
import com.intellij.ui.dsl.builder.actionListener
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import okhttp3.OkHttpClient
import okhttp3.Request
import okhttp3.RequestBody.Companion.toRequestBody
import javax.swing.JButton
import javax.swing.JComponent

@Suppress("UnstableApiUsage")
class SettingsComponent {

    private val openAiButton = JButton("Instant OpenAi Integration", PickYourAutocompletionIcons.AddOpenAi)
    private val huggingFaceButton = JButton(
        "Instant HuggingFace Integration",
        PickYourAutocompletionIcons.AddHuggingFace
    )
    val panel: DialogPanel = panel {
        row {
            text(
                "You don't have any defined entry point for this plugin. Without that autocompletion won't work.",
                DEFAULT_COMMENT_WIDTH
            )

        }
        row {
            text(
                "Use one of existing templates to quickly configure this plugin.",
                DEFAULT_COMMENT_WIDTH
            )
        }
        group("Instant Templates") {
            row {
                cell(openAiButton)
                    .horizontalAlign(HorizontalAlign.FILL)
                    .actionListener { _, _ -> addOpenAiIntegration() }
                cell()
            }.layout(RowLayout.PARENT_GRID)
            row {
                cell(huggingFaceButton)
                    .horizontalAlign(HorizontalAlign.FILL)
                    .actionListener { _, _ -> addHuggingFaceIntegration() }
                cell()
            }.layout(RowLayout.PARENT_GRID)
        }
    }

    private fun addOpenAiIntegration() {
        InstantIntegrationDialog("OpenAi Integration").apply {
            if(!showAndGet())
                return

            // validate OpenAi api key
            val response = OkHttpClient()
                .newCall(
                    Request.Builder()
                        .url("https://api.openai.com/v1/engines")
                        .addHeader("Authorization", "Bearer ${this.apiKey}")
                        .build()
                )
                .execute()

            if (!response.isSuccessful)
                return showWarning(
                    "Failed to authenticate with OpenAi.",
                    response.body?.string() ?: "",
                    openAiButton
                )

            // create ApiKey
            val apiKey = uniqueApiKey("OpenAi Api Key", this.apiKey)
            SettingsState.instance.passwords.add(apiKey)
            val entryPoint = WebhookIntegration.openAi(apiKey).apply {
                name = "Open Ai Integration - cushman-codex"
            }
            SettingsState.instance.entryPoints.add(entryPoint)

            reloadData()
        }
    }

    private fun addHuggingFaceIntegration() {
        InstantIntegrationDialog("Hugging Face Integration").apply {
            if(!showAndGet())
                return

            // validate OpenAi api key
            val response = OkHttpClient()
                .newCall(
                    Request.Builder()
                        .url("https://api-inference.huggingface.co/models/gpt2")
                        .addHeader("Authorization", "Bearer ${this.apiKey}")
                        .post("Test request".toRequestBody())
                        .build()
                )
                .execute()

            if (!response.isSuccessful)
                return showWarning(
                    "Failed to authenticate with Hugging Face.",
                    response.body?.string() ?: "",
                    huggingFaceButton
                )

            // create ApiKey
            val apiKey = uniqueApiKey("Hugging Face Api Key", this.apiKey)
            SettingsState.instance.passwords.add(apiKey)
            val entryPoint = WebhookIntegration.huggingface(apiKey).apply {
                name = "Hugging Face Integration - GPT-Neo-1.3B Code Clippy"
            }
            SettingsState.instance.entryPoints.add(entryPoint)

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

    private fun uniqueApiKey(name: String, password: String) = ApiKey.create(
        name.let { name ->
            // make sure that it is having unique id, todo - move it to separate function
            var newName = name
            var newId = "pwd." + newName.lowercase().replace("\\s".toRegex(), "_")
            while (SettingsState.instance.passwords.find { it.id == newId } != null) {
                newName += " new"
                newId = "pwd." + newName.lowercase().replace("\\s".toRegex(), "_")
            }
            newName
        }, password
    )

    private fun reloadData() {
        EntryPointsConfigurable.instance?.reset()
        PasswordsConfigurable.instance?.reset()
        PromptBuildersConfigurable.instance?.reset()
        PredictorProviderService.instance.reload()
    }

    val preferredFocusedComponent: JComponent?
        get() = null

}