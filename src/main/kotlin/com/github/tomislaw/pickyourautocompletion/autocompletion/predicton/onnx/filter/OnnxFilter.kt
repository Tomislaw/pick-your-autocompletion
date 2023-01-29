package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.onnx.filter

interface OnnxFilter {
    fun filter(logits: Iterable<Pair<Int, Float>>): Iterable<Pair<Int, Float>>
}