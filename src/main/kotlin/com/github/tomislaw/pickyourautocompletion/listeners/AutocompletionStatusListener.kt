package com.github.tomislaw.pickyourautocompletion.listeners

import com.intellij.openapi.vcs.changes.committed.CommittedChangesListener
import com.intellij.util.messages.Topic
import com.intellij.util.messages.Topic.ProjectLevel

interface AutocompletionStatusListener {

    fun onError(throwable: Throwable) ;

    companion object {
        @ProjectLevel
        val TOPIC = Topic(AutocompletionStatusListener::class.java, Topic.BroadcastDirection.NONE)
    }
}