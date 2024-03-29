package com.github.tomislaw.pickyourautocompletion.autocompletion.template

class VariableTemplateParser : TemplateParser {

    companion object {
        private const val NOT_FOUND = ""
        private val matcher = "\\$\\{[\\w\\.]+}".toRegex()
        fun parse(stringToParse: String, property: String, value: String) =
            stringToParse.replace("\${$property}", value)
    }

    private val variables = HashMap<String, String>()

    fun setVariable(key: String, value: String) {
        variables[key] = value
    }

    fun removeVariable(key: String) {
        variables.remove(key)
    }

    // replace variable ${var} in template with one from dictionary
    override fun parse(template: String): String = matcher.replace(template) { match ->
        match.value.removePrefix("\${").removeSuffix("}")
            .let { variable ->
                variables.getOrDefault(variable, NOT_FOUND)
            }
    }

}