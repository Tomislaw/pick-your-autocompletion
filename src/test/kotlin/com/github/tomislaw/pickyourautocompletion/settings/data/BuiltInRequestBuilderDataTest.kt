package com.github.tomislaw.pickyourautocompletion.settings.data

import org.junit.Assert
import org.junit.Test


class BuiltInRequestBuilderDataTest
{
    @Test
    fun fromProperties(){
        val data = BuiltInRequestBuilderData.fromProperties("default","test","test")

        Assert.assertEquals("input_ids", data.inputOutput.inputIds)
        Assert.assertEquals("attention_mask", data.inputOutput.attentionMask)
        Assert.assertEquals(40, data.inputOutput.cache.size)
    }
}