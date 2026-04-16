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
        Log.d(TAG, "Message received from: ${remoteMessage.from}")
        
        val payload = Dictionary()
        for ((key, value) in remoteMessage.data) {
            payload[key] = value
        }

        // Add to queue for the Godot Activity to drain
        MessagingEventQueue.addMessage(payload)
    }

    override fun onNewToken(token: String) {
        Log.d(TAG, "New token: $token")
        MessagingEventQueue.addToken(token)
    }
}
