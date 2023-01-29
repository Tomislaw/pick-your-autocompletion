package com.github.tomislaw.pickyourautocompletion.listeners

import com.intellij.util.messages.Topic
import com.intellij.util.messages.Topic.ProjectLevel

interface AutocompletionStatusListener {
    fun onError(throwable: Throwable){}

    fun onLiveAutocompletionChanged(){}

    companion object {
        @ProjectLevel
        val TOPIC = Topic(AutocompletionStatusListener::class.java, Topic.BroadcastDirection.NONE)
    }
}