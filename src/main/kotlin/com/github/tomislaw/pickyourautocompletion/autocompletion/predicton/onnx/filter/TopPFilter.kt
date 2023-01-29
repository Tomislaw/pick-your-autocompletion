package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.onnx.filter


class TopPFilter(
    private val topP: Float
): OnnxFilter {
    override fun filter(logits: Iterable<Pair<Int, Float>>): Iterable<Pair<Int, Float>> {
        if(topP <=0)
            return logits
        var accumulatedWeight = 0f
        return logits.fold(ArrayList()) { topList, candidate ->
            if (accumulatedWeight < topP || candidate.second > topList.last().second ) {
                topList.add(candidate)
                accumulatedWeight += candidate.second
                topList.sortByDescending { it.second }
                if (topList.last().second < candidate.second ) {
                    topList.removeLast()
                    accumulatedWeight -= topList.last().second
                }
            }
            topList
        }
    }
}