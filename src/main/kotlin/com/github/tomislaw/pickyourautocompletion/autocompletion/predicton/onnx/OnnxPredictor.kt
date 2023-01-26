package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.onnx

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import ai.onnxruntime.OrtSession.RunOptions
import com.github.tomislaw.pickyourautocompletion.autocompletion.OnnxModelService
import com.github.tomislaw.pickyourautocompletion.autocompletion.context.Prompt
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.Predictor
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.onnx.filter.NormalizeFilter
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.onnx.filter.RandomWeightFilter
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.onnx.filter.TopKFilter
import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.onnx.filter.TopPFilter
import com.github.tomislaw.pickyourautocompletion.settings.data.BuiltInRequestBuilderData
import com.intellij.openapi.components.service

class OnnxPredictor(request: BuiltInRequestBuilderData) : Predictor {

    private val onnxSampler: OnnxSampler?
    private val onnxGenerator: OnnxGenerator?
    private val tokenizer: HuggingFaceTokenizer?

    private var runOptions: RunOptions? = null

    private lateinit var additionalStopTokens: LongArray
    private val maxTokens = request.maxTokens

    init {
        val onnxService = service<OnnxModelService>()
        if (onnxService.environment == null || onnxService.session == null || onnxService.tokenizer == null) {
            onnxSampler = null
            onnxGenerator = null
            tokenizer = null
        } else {
            onnxSampler = OnnxSampler(
                environment = onnxService.environment!!,
                session = onnxService.session!!,
                inputIdsProperty = "input_ids",
                attentionMaskProperty = "attention_mask",
                cacheKeys = OnnxSampler.generateCacheKeys(
                    "past_key_values.\${id}.key",
                    "present.\${id}.key", 20
                ) + OnnxSampler.generateCacheKeys(
                    "past_key_values.\${id}.value",
                    "present.\${id}.value", 20
                ),
                outputIdsProperty = "logits",
            )
            onnxGenerator = OnnxGenerator(
                onnxSampler,
                listOf(
                    TopKFilter(request.topK),
                    NormalizeFilter(request.temperature),
                    TopPFilter(request.topP),
                    RandomWeightFilter(1)
                )
            )
            tokenizer = onnxService.tokenizer!!
            additionalStopTokens =
                tokenizer.batchEncode(request.stopSequences.toTypedArray()).map { it.ids.last() }.toLongArray()
        }

    }

    override val supportMultiple: Boolean
        get() = true

    override suspend fun predictMultiple(
        prompt: Prompt,
        count: Int,
        tokens: Int,
        stop: List<String>,
    ): Result<List<String>> {
        return if (onnxGenerator == null)
            Result.success(listOf())
        else
            runCatching {
                runOptions?.setTerminate(true)
                runOptions = RunOptions()

                val encoding = tokenizer!!.encode(prompt.text)
                val ids = Array(count) { encoding.ids }
                val mask = Array(count) { encoding.attentionMask }

                val stopTokens = additionalStopTokens + stop.map { tokenizer.encode(it).ids.last() }.toLongArray()
                val result = onnxGenerator!!.generate(ids, mask)
                    .next(tokens.coerceAtMost(if (maxTokens < 0) Int.MAX_VALUE else maxTokens), stopTokens)
                tokenizer.batchDecode(result.toTypedArray()).toList()
            }
    }

    override suspend fun predict(prompt: Prompt, tokens: Int, stop: List<String>): Result<String> {
        return predictMultiple(prompt, 1, tokens, stop).mapCatching { it.firstOrNull() ?: "" }
    }


}