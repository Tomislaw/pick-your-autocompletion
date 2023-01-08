//package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.onnx
//
//import ai.djl.huggingface.tokenizers.HuggingFaceTokenizer
//import ai.onnxruntime.OrtEnvironment
//import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.onnx.filter.NormalizeFilter
//import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.onnx.filter.RandomWeightFilter
//import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.onnx.filter.TopKFilter
//import com.github.tomislaw.pickyourautocompletion.autocompletion.predicton.onnx.filter.TopPFilter
//import org.junit.Test
//import java.nio.file.Paths
//
//
//class OnnxSamplerTest {
//
//    val message = """
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
////    val message = "A"
//
//    @Test
//    suspend fun test() {
//        val env = OrtEnvironment.getEnvironment()
//        val session = env.createSession("C:/Users/TStan/Pulpit/onnx/decoder_with_past_model.onnx")
////        val session = env.createSession("C:/Users/TStan/Pulpit/onnx/codegen-350M-multi.onnx")
//
//        val tokenizer = HuggingFaceTokenizer.newInstance(Paths.get("C:/Users/TStan/Pulpit/onnx/tokenizer.json"))
//
//        val sampler = OnnxSampler(
//            environment = env,
//            session = session,
//            inputIdsProperty = "input_ids",
//            attentionMaskProperty = "attention_mask",
//            cacheKeys = OnnxSampler.generateCacheKeys(
//                "past_key_values.\${id}.key",
//                "present.\${id}.key", 20
//            ) + OnnxSampler.generateCacheKeys(
//                "past_key_values.\${id}.value",
//                "present.\${id}.value", 20
//            ),
//            outputIdsProperty = "logits",
//        )
//
//        val generator = OnnxGenerator(
//            sampler,
//            listOf(
//                TopKFilter(5),
//                NormalizeFilter(1f),
//                TopPFilter(0.3f),
//                RandomWeightFilter(1)
//            )
//        )
//
//
//        val encoding1 = tokenizer.batchEncode(
//            (0..4).map { message }.toTypedArray(), true
//        )
//
//        var forward = generator.generate(
//            encoding1.map { it.ids }.toTypedArray(),
//            encoding1.map { it.attentionMask }.toTypedArray()
//        )
//
//
//        val decoded = forward.next(200, tokenizer.encode("}").ids).map {
//            println(message + "|" + tokenizer.decode(it))
//        }
//
////        val result = sampler.sample(OnnxSampler.Input(arrayOf(encoding1.ids), arrayOf(encoding1.attentionMask)))
////
////        val result2 = sampler.sample(
////            OnnxSampler.Input(
////                arrayOf(encoding2.ids),
////                arrayOf(encoding2.attentionMask),
////                result.presentValues
////            )
////        )
////        val a = 0
//
//    }
//}