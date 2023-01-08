package com.github.tomislaw.pickyourautocompletion.autocompletion.predicton

import org.junit.Assert
import org.junit.Test

class PredictionCacheTest {

    @Test
    fun nextAndPreviousTest() {
        val cache = PredictionCache()
        cache.add("test1")
        cache.add("test2")
        cache.add("test3")

        Assert.assertTrue(cache.hasPrevious())
        Assert.assertFalse(cache.hasNext())

        Assert.assertEquals("test2", cache.previous())
        Assert.assertTrue(cache.hasPrevious())
        Assert.assertTrue(cache.hasNext())

        Assert.assertEquals("test3", cache.next())
        Assert.assertTrue(cache.hasPrevious())
        Assert.assertFalse(cache.hasNext())

        Assert.assertEquals("test2", cache.previous())
        Assert.assertTrue(cache.hasPrevious())
        Assert.assertTrue(cache.hasNext())

        Assert.assertEquals("test1", cache.previous())
        Assert.assertFalse(cache.hasPrevious())
        Assert.assertTrue(cache.hasNext())
    }

    @Test
    fun clearCacheTest() {
        val cache = PredictionCache()
        cache.setEditorOffset(null,0)
        cache.add("test1")
        cache.setEditorOffset(null,1)
        cache.add("test2")
        cache.setEditorOffset(null,1)
        cache.add("test3")

        Assert.assertTrue(cache.hasPrevious())
        Assert.assertFalse(cache.hasNext())

        Assert.assertEquals("test2", cache.previous())
        Assert.assertFalse(cache.hasPrevious())
        Assert.assertTrue(cache.hasNext())

        Assert.assertEquals("test3", cache.next())
        Assert.assertTrue(cache.hasPrevious())
        Assert.assertFalse(cache.hasNext())

        Assert.assertEquals("test2", cache.previous())
        Assert.assertFalse(cache.hasPrevious())
        Assert.assertTrue(cache.hasNext())

        cache.setEditorOffset(null,0)
        Assert.assertFalse(cache.hasPrevious())
        Assert.assertFalse(cache.hasNext())
    }

    @Test
    fun previous() {
    }
}