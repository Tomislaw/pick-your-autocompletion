package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.onnx

import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import ai.onnxruntime.OrtSession.RunOptions
import ai.onnxruntime.OrtUtil
import com.github.tomislaw.pickyourautocompletion.autocompletion.template.VariableTemplateParser
import com.jetbrains.rd.util.concurrentMapOf
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.runBlocking

class OnnxSampler(
    private val environment: OrtEnvironment,
    private val session: OrtSession,

    private val inputIdsProperty: String,
    private val attentionMaskProperty: String,
    private val outputIdsProperty: String,
    private val cacheKeys: Map<String, String> = mapOf()
) {

    private val scope = CoroutineScope(Dispatchers.Default)

    init {
        if (inputIdsProperty.isNotEmpty()) {
            if (!session.inputNames.contains(inputIdsProperty)) throw RuntimeException()
        }
        if (attentionMaskProperty.isNotEmpty()) {
            if (!session.inputNames.contains(attentionMaskProperty)) throw RuntimeException()
        }
        cacheKeys.keys.forEach {
            if (!session.inputNames.contains(it)) throw RuntimeException()
        }

        if (outputIdsProperty.isNotEmpty()) {
            if (!session.outputNames.contains(outputIdsProperty)) throw RuntimeException()
        }
        cacheKeys.values.forEach {
            if (!session.outputNames.contains(it)) throw RuntimeException()
        }
    }


    fun sample(input: Input, runOptions: RunOptions): Output {

        val inputs = concurrentMapOf<String, OnnxTensor>()

        val newInputs = if (cacheKeys.isEmpty())
            input.inputIds
                .mapIndexed { index: Int, longs: LongArray -> input.cachedInputs[index] + longs }
                .toTypedArray()
        else input.inputIds

        if (inputIdsProperty.isNotEmpty()) {
            inputs[inputIdsProperty] = OnnxTensor.createTensor(environment, newInputs)
        }

        var newMask = input.attentionMask
            .mapIndexed { index: Int, longs: LongArray -> input.cachedAttentionMask[index] + longs }
            .toTypedArray()

        if (cacheKeys.isNotEmpty() && input.cachedValues.isEmpty()) {
            newMask = newMask.map { longArrayOf(0) + it }.toTypedArray()
        }

        if (attentionMaskProperty.isNotEmpty()) {
            inputs[attentionMaskProperty] = OnnxTensor.createTensor(environment, newMask)
        }

        if (cacheKeys.isNotEmpty()) runBlocking {
            if (input.cachedValues.isEmpty()) {
                cacheKeys.map { (t, _) ->
                    scope.launch {
                        val info = session.inputInfo[t]!!.info
                        // getting errors when casting to TensorInfo, using this as workaround
                        val shape = (info.javaClass.getMethod("getShape").invoke(info) as LongArray).apply {
                            this[0] = input.inputIds.size.toLong()
                            this[2] = 1
                        }
                        // creating tensors might take some time, doing it async sped up a lot
                        inputs[t] = OnnxTensor.createTensor(environment, OrtUtil.newFloatArray(shape))
                    }
                }.forEach { it.join() }
            } else {
                cacheKeys.map { entry ->
                    scope.launch {
                        inputs[entry.key] = (input.cachedValues[entry.value] as OnnxTensor)
                    }
                }.forEach { it.join() }
            }
        }


        val result = session.run(inputs, setOf(outputIdsProperty) + cacheKeys.values, runOptions)

        val outputs = result.get(outputIdsProperty).get().value as Array<Array<FloatArray>>
        val presentValues = cacheKeys.values.associateWith { result.get(it).get() }

        return Output(
            outputs = outputs,
            cachedValues = presentValues,
            cachedInputs = newInputs,
            cachedAttentionMask = newMask,
        )
    }

    class Input(
        val inputIds: Array<LongArray>,
        val attentionMask: Array<LongArray>,
        val cachedInputs: Array<LongArray> = Array(inputIds.size) { longArrayOf() },
        val cachedAttentionMask: Array<LongArray> = Array(attentionMask.size) { longArrayOf() },
        val cachedValues: Map<String, Any> = mapOf(),
    )

    class Output(
        val outputs: Array<Array<FloatArray>>,
        val cachedInputs: Array<LongArray>,
        val cachedAttentionMask: Array<LongArray>,
        val cachedValues: Map<String, Any> = mapOf()
    )


    companion object {
        fun generateCacheKeys(input: String, output: String, count: Int) = (0 until count).map {
            Pair(
                VariableTemplateParser.parse(input, "id", it.toString()),
                VariableTemplateParser.parse(output, "id", it.toString())
            )
        }.toMap()

    }

}
