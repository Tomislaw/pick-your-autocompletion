package com.github.tomislaw.pickyourautocompletion

import com.intellij.openapi.util.IconLoader
import com.intellij.ui.AnimatedIcon

object Icons {

    @JvmField
    val Logo = IconLoader.getIcon("/icons/logo.svg", javaClass)

    @JvmField
    val LogoActionDisabled = IconLoader.getIcon("/icons/logoAction_disabled.svg", javaClass)

    @JvmField
    val LogoAction = IconLoader.getIcon("/icons/logoAction.svg", javaClass)

    @JvmField
    val LogoActionWarning = IconLoader.getIcon("/icons/logoActionWarning.svg", javaClass)

    @JvmField
    val WarningEnabled = AnimatedIcon(500, LogoAction, LogoActionWarning)

    @JvmField
    val WarningDisabled = AnimatedIcon(500, LogoActionDisabled, LogoActionWarning)

    @JvmField
    val AddCli = IconLoader.getIcon("/icons/addCli.svg", javaClass)

    @JvmField
    val AddCliLarge = IconLoader.getIcon("/icons/addCliLarge.svg", javaClass)

    @JvmField
    val AddHuggingFace = IconLoader.getIcon("/icons/addHuggingface.svg", javaClass)

    @JvmField
    val AddHuggingFaceLarge = IconLoader.getIcon("/icons/addHuggingfaceLarge.svg", javaClass)

    @JvmField
    val AddOpenAi = IconLoader.getIcon("/icons/addOpenai.svg", javaClass)

    @JvmField
    val AddOpenAiLarge = IconLoader.getIcon("/icons/addOpenaiLarge.svg", javaClass)

    @JvmField
    val AddWebhook = IconLoader.getIcon("/icons/addWebhook.svg", javaClass)

    @JvmField
    val AddWebhookLarge = IconLoader.getIcon("/icons/addWebhookLarge.svg", javaClass)

    @JvmField
    val Loading0 = IconLoader.getIcon("/icons/loading0.svg", javaClass)

    @JvmField
    val Loading1 = IconLoader.getIcon("/icons/loading1.svg", javaClass)

    @JvmField
    val Loading2 = IconLoader.getIcon("/icons/loading2.svg", javaClass)

    @JvmField
    val Loading3 = IconLoader.getIcon("/icons/loading3.svg", javaClass)

    @JvmField
    val LoadingPrediction = AnimatedIcon(500, Loading0, Loading1, Loading2, Loading3)
}