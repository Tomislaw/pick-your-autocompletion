package com.github.tomislaw.pickyourautocompletion.utils

import org.junit.Assert.assertEquals
import org.junit.Test

class StringUtilsKtTest {
    @Test
    fun isGettingNextLine() {
        assertEquals("\nfunction()", "\nfunction()".firstLine())
        assertEquals(".function()", ".function()\n".firstLine())
        assertEquals("\n", "\n\n\n".firstLine())
        assertEquals(
            ".function()",
            (".function()" +
                    "\n  function()" +
                    "\n  function()").firstLine()
        )
    }

}