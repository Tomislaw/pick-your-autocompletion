package com.github.tomislaw.pickyourautocompletion.settings.component.dialog

import com.github.tomislaw.pickyourautocompletion.Fonts
import com.github.tomislaw.pickyourautocompletion.Icons
import com.github.tomislaw.pickyourautocompletion.localizedText
import com.github.tomislaw.pickyourautocompletion.settings.SettingsStateService
import com.github.tomislaw.pickyourautocompletion.settings.configurable.RequestBuilderConfigurable
import com.intellij.openapi.components.service
import com.intellij.openapi.options.ShowSettingsUtil
import com.intellij.openapi.project.Project
import com.intellij.openapi.ui.DialogWrapper
import com.intellij.ui.JBColor
import com.intellij.ui.dsl.builder.Align
import com.intellij.ui.dsl.builder.DEFAULT_COMMENT_WIDTH
import com.intellij.ui.dsl.builder.panel
import com.intellij.ui.dsl.gridLayout.HorizontalAlign
import com.intellij.ui.paint.LinePainter2D
import com.intellij.ui.paint.RectanglePainter2D
import java.awt.*
import javax.swing.*

class FirstUseDialog(private val project: Project?) : DialogWrapper(project) {

    private val openAiButton = integrationButton(
        "OpenAi Integration (recommended)",
        Icons.AddOpenAiLarge
    ).apply {
        addActionListener {
            InstantIntegrationDialog
                .addOpenAiIntegration(this) { ok -> if (ok) close(0, true) }
        }

    }
    private val huggingFaceButton = integrationButton(
        "HuggingFace Integration",
        Icons.AddHuggingFaceLarge
    ).apply {
        addActionListener {
            InstantIntegrationDialog
                .addHuggingFaceIntegration(this) { ok -> if (ok) close(0, true) }
        }
    }
    private val localMachineIntegration = integrationButton(
        "Built-in Integration",
        Icons.AddCliLarge
    ).apply {
        addActionListener {
            if (BuiltInIntegrationDialog().showAndGet())
                close(0, true)
        }
    }

    private val customIntegration = integrationButton(
        "Custom Integration",
        Icons.AddWebhookLarge
    ).apply {
        addActionListener {
            ShowSettingsUtil.getInstance().showSettingsDialog(project, RequestBuilderConfigurable::class.java)
        }
    }

    private val banner = object : JPanel(BorderLayout()) {
        override fun paintComponent(g: Graphics) {
            super.paintComponent(g)
            val g2d = g as Graphics2D
            val color1 = JBColor(Color(130, 183, 220), Color(17, 64, 132))
            val color2 = color1.darker()
            g2d.paint = GradientPaint(0f, 0f, color1, 0f, height.toFloat(), color2)
            RectanglePainter2D.FILL.paint(
                g2d, .0, .0, width.toDouble(), height.toDouble(), 25.0,
                LinePainter2D.StrokeType.INSIDE, 1.0, RenderingHints.VALUE_ANTIALIAS_ON
            )
            border = BorderFactory.createEmptyBorder(5, 5, 5, 5)
        }
    }.apply {
        add(JPanel().apply {
            add(JLabel(Icons.Logo).apply {
                text = localizedText("plugin.name")
                font = Fonts.Logo
            })
            background = JBColor(Color(0, 0, 0, 0), Color(0, 0, 0, 0))
        }, BorderLayout.PAGE_START)

        add(JPanel(BorderLayout()).apply {
            add(JLabel(localizedText("plugin.version")), BorderLayout.WEST)
            add(JLabel(localizedText("plugin.author")), BorderLayout.EAST)
            background = JBColor(Color(0, 0, 0, 0), Color(0, 0, 0, 0))
        }, BorderLayout.PAGE_END)

    }

    init {
        this.init()
    }

    private fun integrationButton(text: String, icon: Icon): JButton =
        JButton(text, icon).apply {
            verticalTextPosition = SwingConstants.BOTTOM
            horizontalTextPosition = SwingConstants.CENTER
            minimumSize = Dimension(100, 100)
        }

    override fun createActions(): Array<Action> {
        return arrayOf()
    }

    override fun createCenterPanel(): JComponent = panel {
        row { cell(banner).horizontalAlign(HorizontalAlign.FILL) }

        separator()

        row {
            comment(localizedText("dialog.firstUse"), DEFAULT_COMMENT_WIDTH)
        }

        val state = service<SettingsStateService>().state.autocompletionData
        row {
            cell(JLabel(localizedText("dialog.alreadyConfigured")).apply {
                foreground = JBColor.green
            })
        }.enabled(state.isConfigured)

        separator()

        row {
            cell(JPanel(GridLayout(2, 2)).apply {
                add(openAiButton)
                add(huggingFaceButton)
                add(localMachineIntegration)
                add(customIntegration)
            }).align(Align.FILL)
        }.resizableRow()
    }
}