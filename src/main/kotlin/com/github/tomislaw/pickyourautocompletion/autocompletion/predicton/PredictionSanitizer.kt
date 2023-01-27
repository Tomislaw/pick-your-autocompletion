package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton

import com.github.tomislaw.pickyourautocompletion.settings.data.PredictionSanitizerData

class PredictionSanitizer(
    private val data: PredictionSanitizerData
) {

    fun sanitize(code: String, stopList: Collection<String>): String {
        if (!data.smartStopTokens && data.maxLines < 0)
            return code.trimEnd()

        var lineCount = 0
        for (i in code.indices) {

            if (data.maxLines >= 0) {
                if (i != 0 && code[i] == '\n')
                    lineCount += 1
                if (data.maxLines in 0..lineCount)
                    return code.substring(0, i).trimEnd()
            }

            if (data.smartStopTokens) {
                for (stop in stopList) {
                    if (code.regionMatches(i, stop, 0, stop.length))
                        return code.substring(0, i).trimEnd()
                }
            }
        }
        return code.trimEnd()
    }
}