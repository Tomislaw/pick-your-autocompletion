package com.github.tomislaw.pickyourautocompletion

import java.awt.Font

object Fonts {
    @JvmField
    val Logo = getFont("fonts/Roboto-Medium.ttf")
    private fun getFont(name: String): Font = runCatching {
        val font = Fonts::class.java.classLoader.getResourceAsStream(name)
        Font.createFont(Font.PLAIN, font).deriveFont(Font.PLAIN, 24f)
    }.getOrElse {
        Font("serif", Font.PLAIN, 24)
    }
}
