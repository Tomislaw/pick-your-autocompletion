package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton

import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.sync.Mutex
import kotlinx.coroutines.sync.withLock
import java.util.concurrent.Executors
import java.util.concurrent.Future
import kotlin.math.max


class DelayingTaskExecutor<T> {

    private val dispatcher = Executors.newSingleThreadExecutor()
    private val mutex = Mutex()

    private var future: Future<T>? = null

    private var awaitTime = 0L
    private var finishTime = 0L

    private fun calculateWaitTime(await: Long, currentTime: Long): Long {
        val wait = max(0, awaitTime - (System.currentTimeMillis() - finishTime))
        finishTime = max(finishTime, currentTime)
        awaitTime = await
        return wait
    }

    @Synchronized
    fun scheduleTask(task: suspend () -> T): Future<T> = runBlocking {
        mutex.withLock {
            future?.cancel(true)
            dispatcher.submit<T> { runBlocking { task.invoke() } }
        }
    }

    @Synchronized
    fun scheduleTask(await: Long = 0, task: suspend () -> T): Future<T> = runBlocking {
        mutex.withLock {

            // cancel previous task
            future?.cancel(true)

            // calculate time to wait
            val timeToWait = calculateWaitTime(await, System.currentTimeMillis())

            // get future task
            val currentFuture = dispatcher.submit<T> {
                Thread.sleep(timeToWait)
                runBlocking {
                    val result = task.invoke()
                    mutex.withLock {
                        finishTime = max(finishTime, System.currentTimeMillis())
                    }
                    result
                }
            }

            currentFuture.apply { future = this }
        }
    }
}