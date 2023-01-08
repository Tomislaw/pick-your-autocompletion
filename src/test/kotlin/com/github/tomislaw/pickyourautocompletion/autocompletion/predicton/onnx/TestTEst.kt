//package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.onnx
//
//import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
//import ai.onnxruntime.OnnxTensor
//import ai.onnxruntime.OrtEnvironment
//import ai.onnxruntime.OrtSession
//import ai.onnxruntime.OrtSession.SessionOptions
//import kotlinx.coroutines.*
//import org.junit.Test
//import java.nio.file.Paths
//import java.util.*
//import kotlin.time.ExperimentalTime
//
//
//class TestTEst {
//
//    val tokenizer = HuggingFaceTokenizer.newInstance(Paths.get("C:/Users/TStan/Pulpit/onnx/tokenizer.json"))
//
//    @OptIn(ExperimentalTime::class)
//    @Test
//    fun test() {
//
//        println("Getting environment model ...")
//        val env = OrtEnvironment.getEnvironment()
//        val providers = OrtEnvironment.getAvailableProviders();
//
//
//        println("Loading model ...")
//        val session = env.createSession("C:/Users/TStan/Pulpit/onnx/codegen-350M-multi.onnx")
//
//        val message =             """
//package com.androidlibrary.scraper
//
//import org.springframework.boot.autoconfigure.SpringBootApplication
//import org.springframework.boot.runApplication
//
//@SpringBootApplication
//class AndroidLibraryScraperApplication
//
//fun main(args: Array<String>) {
//	runApplication<AndroidLibraryScraper"""
//
//        val tokens = tokenizer.encode(message)
//
//
//        val filter = TopKTopPFilter(topK = 1, topP = 0f, maxBranches = 1, temperature = 1f)
//
//        val sampler = object : TopKTopPFilter.Sampler {
//            override fun sample(inputs: List<LongArray>, attentionMasks: List<LongArray>): List<FloatArray> {
//                println("Iteration...")
//                val inputIds = OnnxTensor.createTensor(env, inputs.toTypedArray())
//                val mask = OnnxTensor.createTensor(env, attentionMasks.toTypedArray())
//                val result: OrtSession.Result = session.run(
//                    mapOf(Pair("input_ids", inputIds), Pair("attention_mask", mask))
//                )
//                val resultList = (result.get(0).value as Array<Array<FloatArray>>).toList()
//                return resultList.map { it.last() }
//            }
//        }
//
//        println("Starting prediction ...")
//
//        val scope = CoroutineScope(Dispatchers.Default)
//        runBlocking {
//            (0..0).map {
//                scope.launch {
//                    println(message + tokenizer.decode(filter.filter(tokens.ids, tokens.attentionMask, 5, sampler)))
//                }
//            }.forEach { it.join() }
//        }
//
//
////
////        val encoding = tokenizer.encode("T")
////        val result = filter.filter(encoding.ids, encoding.attentionMask, 10, sampler)
////        val prediction = tokenizer.decode(result)
////        println(prediction)
//    }
//
//
//}