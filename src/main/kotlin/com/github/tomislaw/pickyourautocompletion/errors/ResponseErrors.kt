package com.github.tomislaw.pickyourautocompletion.errors

import com.github.tomislaw.pickyourautocompletion.localizedText

class ResponseFailedError(hostname: String, code: Int, body: String) : Exception(
    localizedText("error.responseNotSuccessful").format(hostname, code, body)
)

class ResponseParsingError(parserPath: String, response: String) : Exception(
    localizedText("error.responseParsing").format(parserPath, response)
)

class ResponseServerUnavailableError (hostname: String) : Exception(
    localizedText("error.responseFailed").format(hostname)
)

class ResponseTimeoutError(hostname: String, timeoutTime: Float) : Exception(
    localizedText("error.responseTimeout").format(hostname, timeoutTime)
)

class ResponseUnknownHostError(hostname: String) : Exception(
    localizedText("error.responseUnknownHost").format(hostname)
)

class ResponseConnectionShutdownError(hostname: String) : Exception(
    localizedText("error.responseConnectionShutdown").format(hostname)
)