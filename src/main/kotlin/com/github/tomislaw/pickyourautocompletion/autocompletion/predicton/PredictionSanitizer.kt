package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton

import com.github.tomislaw.pickyourautocompletion.settings.data.PredictionSanitizerData

class PredictionSanitizer(
    private val data: PredictionSanitizerData
) {

    fun sanitize(code: String, stopList: Collection<String>): String {
        if (!data.contentAwareStopTokenEnabled)
            return code.trimEnd()

        var lineCount = 0
        for (i in code.indices) {

            if (i != 0 && code[i] == '\n')
                lineCount += 1
            if (data.maxPredictionLinesCount in 0..lineCount)
                return code.substring(0, i).trimEnd()

            for (stop in stopList) {
                if (code.regionMatches(i, stop, 0, stop.length))
                    return code.substring(0, i).trimEnd()
            }
        }
        return code.trimEnd()
    }
}