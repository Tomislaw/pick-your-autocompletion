package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.onnx.filter

class NormalizeFilter(private val temperature: Float) : OnnxFilter {
    override fun filter(logits: Iterable<Pair<Int, Float>>): Iterable<Pair<Int, Float>> {
        val temp = temperature.coerceAtLeast(0.0001f)
        val cumSum = logits.fold(0f) { initial, next -> initial + next.second.coerceAtLeast(0f) }
        return logits.map { Pair(it.first, (it.second / cumSum) / temp) }
    }

}