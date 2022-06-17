package com.github.tomislaw.pickyourautocompletion.utils

import com.intellij.util.Base64
import com.intellij.util.xmlb.Converter
import java.nio.charset.Charset

class CharsetSerializer : Converter<Charset>() {
    override fun toString(value: Charset): String = value.name()

    override fun fromString(value: String): Charset = Charset.forName(value)
}

class Base64HeaderListSerializer : Converter<MutableList<Pair<String, String>>>() {
    override fun toString(value: MutableList<Pair<String, String>>): String =
        value.joinToString(separator = ",") {
            Base64.encode(it.first.toByteArray()) + ":" + Base64.encode(it.second.toByteArray())
        }

    override fun fromString(value: String): MutableList<Pair<String, String>> =
        value.split(",").map {
            it.split(":").let { pair ->
                Pair(
                    Base64.decode(pair[0]).decodeToString(),
                    Base64.decode(pair[1]).decodeToString()
                )
            }

        }.toMutableList()

}

class Base64StringSerializer : Converter<String>() {
    override fun toString(value: String): String = Base64.encode(value.toByteArray())

    override fun fromString(value: String): String = Base64.decode(value).decodeToString()

}