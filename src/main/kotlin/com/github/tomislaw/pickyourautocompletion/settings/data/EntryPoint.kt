package com.github.tomislaw.pickyourautocompletion.settings.data


interface EntryPoint {
    val order: Int
    val supportedFiles: Collection<String>
    val promptBuilder: String
    val name: String
    val type: String

    companion object{
        val WEBHOOK = "Webhook"
        val SCRIPT = "Script"
    }

    fun deepCopy(): EntryPoint
}
