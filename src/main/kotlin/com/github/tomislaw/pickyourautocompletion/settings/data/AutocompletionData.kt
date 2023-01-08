package com.github.tomislaw.pickyourautocompletion.settings.data

data class AutocompletionData(
    var builtInRequestBuilderData: BuiltInRequestBuilderData = BuiltInRequestBuilderData(),
    var webRequestBuilderData: WebRequestBuilderData = WebRequestBuilderData(),
    var promptBuilderData: PromptBuilderData = PromptBuilderData(),

    var predictionSanitizerData: PredictionSanitizerData = PredictionSanitizerData(),

    var maxPredictionsInDialog: Int = 4,
    var builderType: BuilderType = BuilderType.Web

) {

    enum class BuilderType {
        Web, BuiltIn
    }

    val isConfigured: Boolean
        get() {
            val requestBuilderConfigured: Boolean = when (builderType) {
                BuilderType.Web -> webRequestBuilderData.isConfigured
                BuilderType.BuiltIn -> builtInRequestBuilderData.isConfigured
            }
            val promptBuilderConfigured = promptBuilderData.isConfigured

            return promptBuilderConfigured && requestBuilderConfigured
        }
}