package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton

import com.github.tomislaw.pickyourautocompletion.utils.result
import kotlinx.coroutines.*
import org.hamcrest.MatcherAssert
import org.hamcrest.Matchers.lessThan
import org.junit.Test
import kotlin.system.measureTimeMillis

class DelayingTaskExecutorTest {

    @Test
    fun isCancellingPreviousTask() = runBlocking {
        val executor = DelayingTaskExecutor()

        val task1 = executor.scheduleTask(250) {
            delay(500)
            "one"
        }

        delay(10)

        val task2 = executor.scheduleTask(250) {
            delay(500)
            "two"
        }

        assert(task1.result().isFailure)
        assert(task2.result().isSuccess)
    }

    @Test
    fun isWaitingAtLeastForDefinedTime() = runBlocking {
        val executor = DelayingTaskExecutor()

        val awaitTime = 500L

        val time = measureTimeMillis {
            val task1 = executor.scheduleTask(awaitTime) { "one" }.result()
            val task2 = executor.scheduleTask(awaitTime) { "two" }.result()
            assert(task1.isSuccess)
            assert(task2.isSuccess)
        }

        assert(time > awaitTime)
    }

    @Test
    fun notCancellingNonCancellableTask() = runBlocking {
        val executor = DelayingTaskExecutor()

        val awaitTime = 500L

        val time = measureTimeMillis {
            val task1 = executor.scheduleTask(awaitTime) {
                withContext(NonCancellable) {
                    delay(awaitTime)
                }
            }.result()
            val task2 = executor.scheduleTask(awaitTime) { "two" }.result()
            assert(task1.isSuccess)
            assert(task2.isSuccess)
        }

        assert(time > awaitTime * 2)
    }

    @Test
    fun isWaitEnoughTimeAfterMultipleInputs() = runBlocking {
        val executor = DelayingTaskExecutor()
        val awaitTime = 1000L

        val mutableList = mutableListOf<Deferred<*>>()

        val time = measureTimeMillis {
            for (i in 0..20) {
                mutableList.add(executor.scheduleTask(awaitTime) { delay(100) })
            }

            delay(10)
            val lastTask = executor.scheduleTask(awaitTime) { delay(100) }

            for (item in mutableList)
                assert(item.result().isFailure)
            assert(lastTask.result().isSuccess)
        }
        assert(time > awaitTime)
        assert(time < awaitTime * 2)
    }

    @Test
    fun doNotWaitIfTimeAlreadyPassed() = runBlocking {
        val executor = DelayingTaskExecutor()

        var time = measureTimeMillis {
            executor.scheduleTask(1000) { "one" }.result()
        }

        MatcherAssert.assertThat(time, lessThan(100))

        delay(1000)

        time = measureTimeMillis {
            executor.scheduleTask(1000) { "one" }.result()
        }
        MatcherAssert.assertThat(time, lessThan(100))
    }

    @Test
    fun asyncSupported() = runBlocking {
        val executor = DelayingTaskExecutor()


        (0..20).map {
            launch {
                executor.scheduleTask {
                    for (y in 1..10000) {
                        delay(1)
                    }
                    ""
                }.invokeOnCompletion { }
            }
        }.joinAll()


        val task = executor.scheduleTask {
            delay(100)
            ""
        }
        assert(task.result().isSuccess)
    }
}