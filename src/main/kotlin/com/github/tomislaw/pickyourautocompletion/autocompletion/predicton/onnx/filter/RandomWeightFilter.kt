package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.onnx.filter

import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
import java.nio.file.Paths
import java.util.*
import kotlin.random.Random

class RandomWeightFilter(
    private val branches: Int,
    private val random: Random = Random(System.currentTimeMillis())
) : OnnxFilter {

    val tokenizer = HuggingFaceTokenizer.newInstance(Paths.get("C:/Users/TStan/Pulpit/onnx/tokenizer.json"))

    override fun filter(logits: Iterable<Pair<Int, Float>>): Iterable<Pair<Int, Float>> {
        val queue = PriorityQueue<Pair<Int, Float>> { o1, o2 -> o2.second.compareTo(o1.second) }
        logits.forEach {
            queue.add(Pair(it.first, random.nextFloat() * it.second))
            println("" + it.second + ":" + tokenizer.decode(longArrayOf(it.first.toLong())))
        }
        return queue.take(branches)
    }

}