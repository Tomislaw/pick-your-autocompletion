package com.github.tomislaw.pickyourautocompletion.settings.component.dialog

import ai.onnxruntime.OrtEnvironment
import com.github.tomislaw.pickyourautocompletion.settings.SettingsStateService
import com.github.tomislaw.pickyourautocompletion.settings.data.AutocompletionData
import com.github.tomislaw.pickyourautocompletion.settings.data.BuiltInRequestBuilderData
import com.github.tomislaw.pickyourautocompletion.settings.data.PromptBuilderData
import com.intellij.openapi.components.service
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import javax.swing.JComponent


class BuiltInIntegrationDialog : DialogWrapper(true) {

    private val modelLocation = TextFieldWithBrowseButton().apply {
        val state = service<SettingsStateService>().state.autocompletionData
        text = state.builtInRequestBuilderData.modelLocation
        addBrowseFolderListener(
            TextBrowseFolderListener(
                FileChooserDescriptor(
                    true,
                    false,
                    false,
                    false,
                    true,
                    false
                ).withShowHiddenFiles(false)
            )
        )

    }
    private val tokenizerLocation = TextFieldWithBrowseButton().apply {
        val state = service<SettingsStateService>().state.autocompletionData
        text = state.builtInRequestBuilderData.tokenizerLocation
        addBrowseFolderListener(
            TextBrowseFolderListener(
                FileChooserDescriptor(
                    true,
                    false,
                    false,
                    false,
                    true,
                    false
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

    override fun showAndGet(): Boolean {
        val ok = super.showAndGet()

        val state = service<SettingsStateService>().state.autocompletionData
        if (ok) {
            state.builtInRequestBuilderData =
                BuiltInRequestBuilderData.fromProperties(
                    builder = "default",
                    model = modelLocation.text,
                    tokenizer = tokenizerLocation.text,
                    device = devices.item.ordinal
                )
            state.promptBuilderData = PromptBuilderData.fromProperties("default")
            state.builderType = AutocompletionData.BuilderType.BuiltIn
        }
        reloadData()
        return ok
    }

    override fun createCenterPanel(): JComponent = panel {
        group("Help") {
            row {
                comment("This is experimental feature. blabla more text")
            }
        }
        group("Model Data") {
            row("Model file") {
                cell(modelLocation).horizontalAlign(HorizontalAlign.FILL)
            }
            row("Tokenizer file") {
                cell(tokenizerLocation).horizontalAlign(HorizontalAlign.FILL)
            }
        }
        group("Device") {
            row {
                cell(devices).horizontalAlign(HorizontalAlign.FILL)
            }

        }
    }

    private fun reloadData() {
        service<SettingsStateService>().settingsChanged()
    }
}