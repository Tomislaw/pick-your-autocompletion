package com.github.tomislaw.pickyourautocompletion.utils

import kotlinx.coroutines.Deferred
import kotlinx.coroutines.coroutineScope
import java.util.concurrent.Future


suspend fun <T> Deferred<T>.result() = kotlin.runCatching { this.await() }

suspend fun <T> Future<T>.result() = coroutineScope { runCatching { this@result.get() } }