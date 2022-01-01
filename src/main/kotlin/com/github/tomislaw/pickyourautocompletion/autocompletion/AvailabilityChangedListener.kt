package com.github.tomislaw.pickyourautocompletion.autocompletion

import org.apache.tools.ant.taskdefs.Available
import java.util.*

interface AvailabilityChangedListener : EventListener {
    fun isAvailable(available: Boolean)
}