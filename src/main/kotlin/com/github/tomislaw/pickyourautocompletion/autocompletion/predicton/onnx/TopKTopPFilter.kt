package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.onnx

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import java.nio.file.Paths
import java.util.PriorityQueue
import kotlin.random.Random

class TopKTopPFilter(
    var topK: Int = 100,
    var topP: Float = 0f,
    var temperature: Float = 1f,
    var maxBranches: Int = 1,
    val random: Random = Random.Default
) {
    fun filter(
        input: LongArray, attentionMask: LongArray, samples: Int, sampler: Sampler
    ): LongArray {
        var logits = listOf(Logits(input.toList(), attentionMask.toList(), 0f))
        for (i in 0 until samples) {
            logits = logits.sample(sampler, temperature).flatten()
                .topKFilter(topK)
                .topPFilter(topP)
                .randomWeightedPriorityQueue(random.nextInt()).take(maxBranches)
        }
        return logits.randomWeightedPriorityQueue(random.nextInt())
            .first().input.let {
                it.subList(input.size, it.size)
            }.toLongArray()
    }

    private fun List<Logits>.sample(sampler: Sampler, temperature: Float = 1f): List<List<Logits>> {
        val inputs = this.map { it.input.toLongArray() }
        val masks = this.map { it.attentionMask.toLongArray() }
        val samples = sampler.sample(inputs, masks)

        return samples.take(this.size).mapIndexed { nodeIndex: Int, floats: FloatArray ->
            floats.mapIndexed { index: Int, weight: Float ->
                val node = this[nodeIndex]
                val weightWithTemperature = weight / temperature.coerceAtLeast(0.0001f)
                Logits(
                    node.input + index.toLong(), node.attentionMask + 1L, weightWithTemperature
                )
            }
        }
    }

    private fun List<Logits>.topKFilter(topK: Int): List<Logits> {
        if (topK <= 0) return this

        return this.fold(ArrayList()) { topList, candidate ->
            if (topList.size < topK || candidate.weight > topList.last().weight) {
                topList.add(candidate)
                topList.sortByDescending { it.weight }
                if (topList.size > topK) topList.removeAt(topK)
            }
            topList
        }
    }


    private fun List<Logits>.topPFilter(topP: Float): List<Logits> {
        if (topP <= 0f) return this

        var accumulatedWeight = 0f
        return this.fold(ArrayList()) { topList, candidate ->
            if (accumulatedWeight < topP || candidate.weight > topList.last().weight) {
                topList.add(candidate)
                accumulatedWeight += candidate.weight
                topList.sortByDescending { it.weight }
                if (topList.size > topK) {
                    topList.removeAt(topK)
                    accumulatedWeight -= candidate.weight
                }
            }
            topList
        }
    }

    private fun List<Logits>.randomWeightedPriorityQueue(seed: Int = 0): PriorityQueue<Logits> {
        val random = Random(seed)
        val queue = PriorityQueue<Logits> { o1, o2 -> o2.weight.compareTo(o1.weight) }
        this.forEach { queue.add(Logits(it.input, it.attentionMask, random.nextFloat() * it.weight))}
        return queue
    }


    private class Logits(
        val input: List<Long>,
        val attentionMask: List<Long>,
        val weight: Float,
    )

    interface Sampler {
        fun sample(inputs: List<LongArray>, attentionMasks: List<LongArray>): List<FloatArray>
    }
}


