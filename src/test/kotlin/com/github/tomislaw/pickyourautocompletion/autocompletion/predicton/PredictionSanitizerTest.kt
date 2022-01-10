package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton

import org.junit.Assert
import org.junit.Test

class PredictionSanitizerTest {

    @Test
    fun testRemovingWhiteSpace() {
        val underTesting = PredictionSanitizer()
        val inputMessage = "some tested code  \n\n  "
        val testedMessage = underTesting.sanitize(inputMessage, emptyList())
        Assert.assertEquals("some tested code", testedMessage)
    }

    @Test
    fun testRemovingAdditionalBrackets() {
        val underTesting = PredictionSanitizer()
        val inputMessage =
            "class Test : Interface<Pair<Int,Int>>(val a: Pair<>){" +
                    "\n    val code = 5" +
                    "\n    val testValue = test[5]" + "\n}" + "\n\n}" + "\n\n}"

        val testedMessage = underTesting.sanitize(inputMessage, emptyList())
        Assert.assertEquals(
            "class Test : Interface<Pair<Int,Int>>(val a: Pair<>){" +
                    "\n    val code = 5" +
                    "\n    val testValue = test[5]" + "\n}", testedMessage
        )
    }

    @Test
    fun testBracketsAndWhiteSpaceOnly() {
        val underTesting = PredictionSanitizer()
        val inputMessage = "}}  \n"
        val testedMessage = underTesting.sanitize(inputMessage, emptyList())
        Assert.assertEquals("", testedMessage)
    }

    @Test
    fun testRemovingAdditionalBracketsXml() {
        val underTesting = PredictionSanitizer()
        val inputMessage =
            "<xml>{some message}</xml><test/><notFinishedXml"

        val testedMessage = underTesting.sanitize(inputMessage, emptyList())
        Assert.assertEquals("<xml>{some message}</xml><test/>", testedMessage)
    }

    @Test
    fun testFull() {
        val underTesting = PredictionSanitizer()
        val inputMessage =
            "some test function()\n" +
                    "{\n" +
                    "  val a = 0\n" +
                    "}\n" +
                    "stop phrase" +
                    "some content"

        val testedMessage = underTesting.sanitize(inputMessage, listOf("stop phrase"))
        Assert.assertEquals(
            "some test function()\n" +
                    "{\n" +
                    "  val a = 0\n" +
                    "}", testedMessage
        )
    }

}