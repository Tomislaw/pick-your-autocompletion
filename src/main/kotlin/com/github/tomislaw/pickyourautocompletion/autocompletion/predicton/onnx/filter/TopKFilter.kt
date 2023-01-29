package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.onnx.filter

class TopKFilter(
    private val topK: Int
) : OnnxFilter {
    override fun filter(logits: Iterable<Pair<Int, Float>>): Iterable<Pair<Int, Float>> {
        if (topK <= 0)
            return logits
        return logits.fold(ArrayList()) { topList, candidate ->
            if (topList.size < topK || candidate.second > topList.last().second) {
                topList.add(candidate)
                topList.sortByDescending { it.second }
                if (topList.size > topK) topList.removeAt(topK)
            }
            topList
        }
    }
}