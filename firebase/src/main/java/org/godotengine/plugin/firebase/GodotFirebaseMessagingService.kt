package org.godotengine.plugin.firebase

import android.util.Log
import com.google.firebase.messaging.FirebaseMessagingService
import com.google.firebase.messaging.RemoteMessage
import org.godotengine.godot.Dictionary

class GodotFirebaseMessagingService : FirebaseMessagingService() {
    companion object {
        private const val TAG = "GodotFirebaseMessaging"
    }

    override fun onMessageReceived(remoteMessage: RemoteMessage) {
        val payload = Dictionary()
        
        // Extract notification content (Title and Body)
        remoteMessage.notification?.let {
            payload["title"] = it.title ?: ""
            payload["body"] = it.body ?: ""
        }

        // Extract custom data fields
        for ((key, value) in remoteMessage.data) {
            payload[key] = value
        }

        // Add to queue and notify the Messaging instance to drain it immediately if active
        MessagingEventQueue.addMessage(payload)
        Messaging.instance?.drainQueue()
    }

    override fun onNewToken(token: String) {
        MessagingEventQueue.addToken(token)
        Messaging.instance?.drainQueue()
    }
}
