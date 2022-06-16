package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton

import com.github.tomislaw.pickyourautocompletion.utils.result
import kotlinx.coroutines.delay
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking
import org.hamcrest.Matchers.*
import org.junit.Assert
import org.junit.Test
import java.util.concurrent.Future
import kotlin.system.measureTimeMillis

class DelayingTaskExecutorTest {

    @Test
    fun isCancellingPreviousTask() = runBlocking {
        val executor = DelayingTaskExecutor<String>()

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
        val executor = DelayingTaskExecutor<String>()

        val awaitTime = 500L

        val time = measureTimeMillis {
            val task1 = executor.scheduleTask(awaitTime) { "one" }.result()
            val task2 = executor.scheduleTask { "two" }.result()
            assert(task1.isSuccess)
            assert(task2.isSuccess)
        }

        assert(time > awaitTime)
    }

    @Test
    fun isWaitEnoughTimeAfterMultipleInputs() = runBlocking {
        val executor = DelayingTaskExecutor<Unit>()
        val awaitTime = 1000L

        val mutableList = mutableListOf<Future<*>>()

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
        val executor = DelayingTaskExecutor<String>()

        var time = measureTimeMillis {
            executor.scheduleTask(1000) { "one" }.result()
        }
        Assert.assertThat(time, lessThan(100))

        delay(1000)

        time = measureTimeMillis {
            executor.scheduleTask(1000) { "one" }.result()
        }
        Assert.assertThat(time, lessThan(100))
    }

    @Test
    fun asyncSupported() = runBlocking {
        val executor = DelayingTaskExecutor<String>()

        for (i in 0..20) {
            launch {
                executor.scheduleTask {
                    var s = ""
                    for (y in 1..10000)
                        s += y
                    s
                }
            }
        }

        delay(10)

        val task = executor.scheduleTask {
            var s = ""
            for (i in 1..10000)
                s += i
            s
        }
        assert(task.result().isSuccess)
    }
}