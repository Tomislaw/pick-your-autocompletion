package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton

import kotlinx.coroutines.*
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.Executors
import kotlin.math.max


class DelayingTaskExecutor {

    private val dispatcher = Executors.newSingleThreadExecutor()
    private var scope = CoroutineScope(dispatcher.asCoroutineDispatcher())

    private val mutex = Mutex()

    private var deferred: Deferred<*>? = null

    private var awaitTime = 0L
    private var finishTime = 0L

    val isActive get() = deferred?.isActive == true

    private fun calculateWaitTime(await: Long): Long {
        val wait = max(0, awaitTime - (System.currentTimeMillis() - finishTime))
        awaitTime = await
        return wait
    }

    private fun cancelTaskInternal() = runBlocking {
        if (deferred?.isActive == true) {
            deferred?.cancel()
            deferred = null
            finishTime = System.currentTimeMillis()
        }
    }

    fun <T> scheduleTask(task: suspend () -> T): Deferred<T> = runBlocking {
        mutex.withLock {
            cancelTaskInternal()
            scope.async { task.invoke() }.apply { deferred = this }
        }
    }


    fun <T> scheduleTask(await: Long = 0, task: suspend () -> T): Deferred<T> = runBlocking {
        mutex.withLock {
            cancelTaskInternal()

            // calculate time to wait
            val timeToWait = calculateWaitTime(await)

            // reset scope if previous crashed
            if (!scope.isActive)
                scope = CoroutineScope(dispatcher.asCoroutineDispatcher())

            // get future task
            scope.async {
                delay(timeToWait)
                val result = task.invoke()
                ensureActive()
                mutex.withLock {
                    finishTime = System.currentTimeMillis()
                }
                result
            }.apply { deferred = this }
        }
    }
}