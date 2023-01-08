package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.onnx

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import ai.djl.util.ClassLoaderUtils
import ai.onnxruntime.OnnxTensor
import ai.onnxruntime.OrtEnvironment
import ai.onnxruntime.OrtSession
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.Predictor
import com.github.tomislaw.pickyourautocompletion.settings.data.BuiltInRequestBuilderData
import java.nio.file.Paths
import kotlin.concurrent.thread


class OnnxPredictorOld(request: BuiltInRequestBuilderData) : Predictor {

    private val env: OrtEnvironment
    private val session: OrtSession
    private val runOptions: OrtSession.RunOptions
    private val tokenizer: HuggingFaceTokenizer
    private val filter: TopKTopPFilter

    init {
        env = OrtEnvironment.getEnvironment()
        val sessionOptions = OrtSession.SessionOptions()
        sessionOptions.setInterOpNumThreads(8)
        sessionOptions.setExecutionMode(OrtSession.SessionOptions.ExecutionMode.PARALLEL)
        sessionOptions.disableProfiling()
        sessionOptions.addCUDA()
        runOptions = OrtSession.RunOptions()

        session = env.createSession("C:/Users/TStan/Pulpit/onnx/codegen-350M-multi.onnx", sessionOptions)
        lateinit var mTokenizer: HuggingFaceTokenizer
        thread {
            Thread.currentThread().contextClassLoader = ClassLoaderUtils::class.java.classLoader
            mTokenizer = HuggingFaceTokenizer.newInstance(Paths.get("C:/Users/TStan/Pulpit/onnx/tokenizer.json"))
        }.join()
        tokenizer = mTokenizer

        filter = TopKTopPFilter(request.topK, request.topP, request.temperature)
    }

    private val sampler = object : TopKTopPFilter.Sampler {
        override fun sample(inputs: List<LongArray>, attentionMasks: List<LongArray>): List<FloatArray> {
            val inputIds = OnnxTensor.createTensor(env, inputs.toTypedArray())
            val mask = OnnxTensor.createTensor(env, attentionMasks.toTypedArray())
            val result: OrtSession.Result = session.run(
                mapOf(Pair("input_ids", inputIds), Pair("attention_mask", mask)), runOptions
            )
            val resultList = (result.get(0).value as Array<Array<FloatArray>>).toList()
            return resultList.map { it.last() }
        }
    }

    override suspend fun predict(codeContext: String, tokens: Int, stop: List<String>): Result<String> {
        val encoded = tokenizer.encode(codeContext)
        return filter.runCatching {
            tokenizer.decode(filter(encoded.ids, encoded.attentionMask, tokens, sampler))
        }
    }

    override fun delayTime(): Long = 0L


}