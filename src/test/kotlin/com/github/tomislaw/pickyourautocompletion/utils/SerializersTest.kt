package com.github.tomislaw.pickyourautocompletion.utils

import org.junit.Assert.*
import org.junit.Test

class SerializersTest {

    @Test
    fun charsetSerializerTest() {

        assertEquals("UTF-16", CharsetSerializer().toString(Charsets.UTF_16))
        assertEquals("ISO-8859-1", CharsetSerializer().toString(Charsets.ISO_8859_1))

        assertEquals(Charsets.UTF_8, CharsetSerializer().fromString("UTF-8"))
        assertEquals(Charsets.US_ASCII, CharsetSerializer().fromString("US-ASCII"))

        assertEquals(
            Charsets.UTF_32LE,
            CharsetSerializer().fromString(
                CharsetSerializer().toString(Charsets.UTF_32LE)
            )
        )
    }

    @Test
    fun headerListSerializerTest() {
        val list = mutableListOf(Pair("Header1", "fagshgr@$#^57484!!;.."), Pair("Header2", "Value2"))

        assertEquals(
            list,
            Base64HeaderListSerializer().fromString(
                Base64HeaderListSerializer().toString(list)
            )
        )
    }

    @Test
    fun base64StringSerializer(){
        val string = "casdgbd(*6u4y3../**-!@!!==,,"

        assertEquals(
            string,
            Base64StringSerializer().fromString(
                Base64StringSerializer().toString(string)
            )
        )
    }

}