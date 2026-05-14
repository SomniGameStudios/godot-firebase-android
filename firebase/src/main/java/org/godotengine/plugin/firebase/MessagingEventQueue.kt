package org.godotengine.plugin.firebase

import org.godotengine.godot.Dictionary

object MessagingEventQueue {
    private val events = mutableListOf<Event>()

    sealed class Event {
        data class MessageReceived(val payload: Dictionary) : Event()
        data class TokenRefreshed(val token: String) : Event()
    }

    fun addMessage(payload: Dictionary) {
        synchronized(events) {
            events.add(Event.MessageReceived(payload))
        }
    }

    fun addToken(token: String) {
        synchronized(events) {
            events.add(Event.TokenRefreshed(token))
        }
    }

    fun drain(): List<Event> {
        return synchronized(events) {
            val copy = events.toList()
            events.clear()
            copy
        }
    }
}
