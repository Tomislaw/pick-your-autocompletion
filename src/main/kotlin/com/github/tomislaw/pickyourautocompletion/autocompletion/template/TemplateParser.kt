package com.github.tomislaw.pickyourautocompletion.autocompletion.template

interface TemplateParser {
    fun parse(template: String) : String
}