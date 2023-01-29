package com.github.tomislaw.pickyourautocompletion.settings.component.builders

import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtProvider
import com.github.tomislaw.pickyourautocompletion.settings.data.BuiltInRequestBuilderData
import com.intellij.openapi.fileChooser.FileChooserDescriptor
import com.intellij.openapi.ui.ComboBox
import com.intellij.openapi.ui.TextBrowseFolderListener
import com.intellij.openapi.ui.TextFieldWithBrowseButton
import com.intellij.ui.components.JBTextArea
import com.intellij.ui.components.JBTextField
import com.intellij.ui.dsl.builder.*
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.table.JBTable
import javax.swing.table.AbstractTableModel

class BuiltInRequestBuilderComponent {

    var builtInRequestData: BuiltInRequestBuilderData
        get() = BuiltInRequestBuilderData(
            modelLocation = modelLocation.text,
            tokenizerLocation = tokenizerLocation.text,
            topK = topK.text.toIntOrNull() ?: 0,
            topP = topP.text.toFloatOrNull() ?: 0f,
            temperature = temperature.text.toFloatOrNull() ?: 0f,
            device = devices.item.ordinal,
            inputOutput = BuiltInRequestBuilderData.InputsOutputs(
                inputIds = inputIds.text,
                attentionMask = attentionMasks.text,
                logits = logits.text,
                cache = cache.toMap()
            ),
            stopSequences = stopSequences.text.split("\n").filter { it.isNotEmpty() }
        )
        set(value) {
            modelLocation.text = value.modelLocation
            tokenizerLocation.text = value.tokenizerLocation
            topK.text = value.topK.toString()
            topP.text = value.topP.toString()
            temperature.text = value.temperature.toString()
            devices.item = OrtProvider.values()[value.device]
            stopSequences.text = value.stopSequences.joinToString("\n")
            inputIds.text = value.inputOutput.inputIds
            attentionMasks.text = value.inputOutput.attentionMask
            logits.text = value.inputOutput.logits

            val copy = ArrayList(value.inputOutput.cache.toList())
            cache.clear()
            copy.forEach { cache.add(it) }
        }

    private val modelLocation = TextFieldWithBrowseButton().apply {
        addBrowseFolderListener(
            TextBrowseFolderListener(
                FileChooserDescriptor(
                    true, false, false, false, true, false
                ).withShowHiddenFiles(false)
            )
        )
    }
    private val tokenizerLocation = TextFieldWithBrowseButton().apply {
        addBrowseFolderListener(
            TextBrowseFolderListener(
                FileChooserDescriptor(
                    true, false, false, false, true, false
                ).withShowHiddenFiles(false)
            )
        )
    }
    private val devices = ComboBox(ortDevices)

    private val stopSequences = JBTextArea()

    private val topP = JBTextField()
    private val topK = JBTextField()
    private val temperature = JBTextField()
    private val ortDevices get() = OrtEnvironment.getAvailableProviders().toTypedArray()

    private val cache = mutableListOf<Pair<String, String>>()
    private val cacheTableModel = CacheTableModel()
    private val cacheTable = JBTable(cacheTableModel)

    private val inputIds = JBTextField()
    private val attentionMasks = JBTextField()
    private val logits = JBTextField()
    fun createComponent(panel: Panel) = panel.panel {
        row("Model file") {
            cell(modelLocation).horizontalAlign(HorizontalAlign.FILL).comment("If empty then using built-in model")
        }
        row("Tokenizer file") {
            cell(tokenizerLocation).horizontalAlign(HorizontalAlign.FILL)
                .comment("If empty then using built-in tokenizer")
        }
        row("Device") {
            cell(devices).horizontalAlign(HorizontalAlign.FILL)
        }
        group("Stop Tokens"){
            row {
                cell(stopSequences).horizontalAlign(HorizontalAlign.FILL).comment("One token per line.")
            }
        }
        group("Filtering") {
            row("Top K") {
                cell(topK).horizontalAlign(HorizontalAlign.FILL)
            }
            row("Top P") {
                cell(topP).horizontalAlign(HorizontalAlign.FILL)
            }
            row("Temperature") {
                cell(temperature).horizontalAlign(HorizontalAlign.FILL)
            }
        }
        collapsibleGroup("Inputs And Outputs") {
            group("Inputs") {
                row("Input ids") {
                    cell(inputIds).horizontalAlign(HorizontalAlign.FILL).comment("[batch_size,tokens_size]{Int64}")
                }
                row("Attention masks") {
                    cell(attentionMasks).horizontalAlign(HorizontalAlign.FILL)
                        .comment("[batch_size,tokens_size+past_tokens_size]{Int64}")
                }
            }
            group("Outputs") {
                row("Logits") {
                    cell(logits).horizontalAlign(HorizontalAlign.FILL).comment("[batch_size,tokens_size+past_tokens_size]{Float32}")
                }
            }
            group("Cache") {
                row {
                    cell(cacheTable).horizontalAlign(HorizontalAlign.FILL).comment("[batch_size,Any...]{Any}")
                }
            }

        }
    }

    inner class CacheTableModel : AbstractTableModel() {
        override fun getRowCount(): Int = cache.size + 1
        override fun getColumnCount(): Int = 2
        override fun getColumnName(columnIndex: Int): String = when (columnIndex) {
            0 -> "Past Cache Key"
            else -> "Present Cache Key"
        }

        override fun getColumnClass(columnIndex: Int): Class<*> = String::class.java
        override fun isCellEditable(rowIndex: Int, columnIndex: Int): Boolean = true
        override fun getValueAt(rowIndex: Int, columnIndex: Int): Any = when {
            rowIndex >= cache.size -> ""
            columnIndex == 0 -> cache[rowIndex].first
            else -> cache[rowIndex].second
        }

        override fun setValueAt(aValue: Any?, rowIndex: Int, columnIndex: Int) {
            if (rowIndex >= cache.size) {
                // do nothing if empty
                if (aValue == null || aValue.toString().isBlank()) return

                // add new row
                when (columnIndex) {
                    0 -> cache.add(Pair(aValue.toString(), ""))
                    else -> cache.add(Pair("", aValue.toString()))
                }

            } else {
                when (columnIndex) {
                    0 -> cache[rowIndex] = Pair(aValue.toString(), cache[rowIndex].second)

                    else -> cache[rowIndex] = Pair(cache[rowIndex].first, aValue.toString())
                }
                // if all columns in row are empty then remove header
                if (cache[rowIndex].first.isBlank() && cache[rowIndex].second.isBlank()) {
                    cache.removeAt(rowIndex)
                }
            }
        }
    }

}