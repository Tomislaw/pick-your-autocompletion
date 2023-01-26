package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.onnx

import ai.onnxruntime.OrtSession.RunOptions
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.onnx.filter.OnnxFilter
import kotlinx.coroutines.*
import kotlin.coroutines.coroutineContext

class OnnxGenerator(
    private val sampler: OnnxSampler,
    private val filters: List<OnnxFilter>,
    private val maxTokens: Int = -1,
) {

    private var runOptions: RunOptions? = null
    fun generate(inputs: Array<LongArray>, attentionMask: Array<LongArray>): ForwardGenerator {
        runOptions?.close()
        runOptions = RunOptions()
        val input = OnnxSampler.Input(inputs, attentionMask)
        return ForwardGenerator(input)
    }

    fun filter(result: Iterable<Iterable<Pair<Int, Float>>>) =
        result.map {
            var logits = it
            for (filter in filters) {
                logits = filter.filter(logits)
            }
            logits
        }


    inner class ForwardGenerator internal constructor(
        private var input: OnnxSampler.Input,
        private var generatedTokens: Int = 0
    ) {
        private val closedList = input.inputIds.map { false }.toTypedArray()

        fun hasNext(): Boolean = (maxTokens < 0 || generatedTokens < maxTokens)
                && input.inputIds.isNotEmpty()
                && closedList.contains(false)

        suspend fun next(stopTokens: LongArray): List<Long?> {
            coroutineContext.ensureActive()

            generatedTokens++
            val options = runOptions ?: return listOf()

            lateinit var result: OnnxSampler.Output
            val job = CoroutineScope(coroutineContext).launch {
                result = sampler.sample(input, options)
            }

            while (!job.isCompleted) {
                if (!coroutineContext.isActive) {
                    options.setTerminate(true)
                } else {
                    withContext(NonCancellable) {
                        delay(50)
                    }
                }
            }

            // filter logits and select token
            val ids = result.outputs.mapIndexed { index: Int, output: Array<FloatArray> ->
                coroutineContext.ensureActive()

                var logits: Iterable<Pair<Int, Float>> = output.logits()
                for (filter in filters) {
                    logits = filter.filter(logits)
                }

                val token = logits.first().first.toLong()
                if (stopTokens.contains(token))
                    closedList[index] = true

                token
            }


            val inputIds = ids.map { longArrayOf(it) }.toTypedArray()
            val attentionMasks = ids.map { longArrayOf(1) }.toTypedArray()

            // create new input for next pass
            input = OnnxSampler.Input(
                inputIds, attentionMasks, result.cachedInputs, result.cachedAttentionMask,
                result.cachedValues
            )

            return closedList.mapIndexed { index: Int, b: Boolean -> if (b) null else ids[index] }
        }

        suspend fun next(count: Int, stopTokens: LongArray = longArrayOf()): List<LongArray> {
            val result = MutableList(input.inputIds.size) { longArrayOf() }
            for (i in 0 until count) {
                if (!hasNext())
                    break
                next(stopTokens).forEachIndexed { index, l -> l?.apply { result[index] = result[index] + l } }
            }
            return result
        }

    }

    private fun Array<FloatArray>.logits() =
        this.last().mapIndexed { index: Int, fl: Float ->
            Pair(index, fl)
        }
}
