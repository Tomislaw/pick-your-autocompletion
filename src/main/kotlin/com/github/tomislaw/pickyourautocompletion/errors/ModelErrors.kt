package com.github.tomislaw.pickyourautocompletion.errors

import com.github.tomislaw.pickyourautocompletion.localizedText

class ModelInvalidInput : ShowConfigError, Exception(
    localizedText("error.modelInvalidInput").format()
)

class ModelInvalidOutputError : ShowConfigError, Exception(
    localizedText("error.modelInvalidOutput").format()
)

class ModelRuntimeError : ShowConfigError, Exception(
    localizedText("error.modelRuntime").format()
)

class ModelFailedToLoadError(th: Throwable) : ShowConfigError, Exception(
    localizedText("error.modelFailedToLoad").format(), th
)

class TokenizerFailedToLoadError : ShowConfigError, Exception(
    localizedText("error.tokenizerFailedToLoad").format()
)

class ModelFailedToDownload(th: Throwable) : ShowConfigError, Exception(
    localizedText("error.modelFailedToDownload").format(), th
)