package com.github.tomislaw.pickyourautocompletion.settings.data

data class PredictionSanitizerData(
    var removeSameTrailingText: Boolean = true,
    var contentAwareStopTokenEnabled: Boolean = true,
    var maxPredictionLinesCount: Int = 4,
    var stopTokens: List<String> =  listOf()
)