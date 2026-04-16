package org.godotengine.plugin.firebase

import android.Manifest
import android.app.Activity
import android.content.pm.PackageManager
import android.os.Build
import android.util.Log
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import com.google.firebase.messaging.FirebaseMessaging
import org.godotengine.godot.Dictionary
import org.godotengine.godot.plugin.SignalInfo

class Messaging(private val plugin: FirebasePlugin) {
    companion object {
        private const val TAG = "GodotFirebaseMessaging"
    }

    private var activity: Activity? = null

    fun init(activity: Activity) {
        this.activity = activity
    }

    fun onMainResume() {
        drainQueue()
    }
    
    private fun drainQueue() {
        val events = MessagingEventQueue.drain()
        for (event in events) {
            when (event) {
                is MessagingEventQueue.Event.MessageReceived -> {
                    plugin.emitGodotSignal("messaging_notification_received", event.payload)
                }
                is MessagingEventQueue.Event.TokenRefreshed -> {
                    plugin.emitGodotSignal("messaging_token_received", event.token)
                }
            }
        }
    }

    fun messagingSignals(): MutableSet<SignalInfo> {
        val signals: MutableSet<SignalInfo> = mutableSetOf()
        signals.add(SignalInfo("messaging_token_received", String::class.java))
        signals.add(SignalInfo("messaging_notification_received", Dictionary::class.java))
        signals.add(SignalInfo("messaging_permission_result", Boolean::class.javaObjectType))
        signals.add(SignalInfo("messaging_topic_subscribe_success", String::class.java))
        signals.add(SignalInfo("messaging_topic_subscribe_failure", String::class.java))
        signals.add(SignalInfo("messaging_topic_unsubscribe_success", String::class.java))
        signals.add(SignalInfo("messaging_topic_unsubscribe_failure", String::class.java))
        signals.add(SignalInfo("messaging_token_delete_success"))
        signals.add(SignalInfo("messaging_token_delete_failure", String::class.java))
        return signals
    }

    fun getToken() {
        FirebaseMessaging.getInstance().token.addOnCompleteListener { task ->
            if (!task.isSuccessful) {
                Log.w(TAG, "Fetching FCM registration token failed", task.exception)
                return@addOnCompleteListener
            }

            val token = task.result
            if (token != null) {
                plugin.emitGodotSignal("messaging_token_received", token)
            }
        }
    }

    fun deleteToken() {
        FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Token deleted successfully")
                plugin.emitGodotSignal("messaging_token_delete_success")
            } else {
                Log.w(TAG, "Token deletion failed", task.exception)
                plugin.emitGodotSignal("messaging_token_delete_failure", task.exception?.message ?: "Unknown error")
            }
        }
    }

    fun subscribeToTopic(topic: String) {
        FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Subscribed to topic: $topic")
                plugin.emitGodotSignal("messaging_topic_subscribe_success", topic)
            } else {
                Log.w(TAG, "Subscription to topic failed", task.exception)
                plugin.emitGodotSignal("messaging_topic_subscribe_failure", task.exception?.message ?: "Unknown error")
            }
        }
    }

    fun unsubscribeFromTopic(topic: String) {
        FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).addOnCompleteListener { task ->
            if (task.isSuccessful) {
                Log.d(TAG, "Unsubscribed from topic: $topic")
                plugin.emitGodotSignal("messaging_topic_unsubscribe_success", topic)
            } else {
                Log.w(TAG, "Unsubscription from topic failed", task.exception)
                plugin.emitGodotSignal("messaging_topic_unsubscribe_failure", task.exception?.message ?: "Unknown error")
            }
        }
    }

    fun hasPermission(): Boolean {
        if (Build.VERSION.SDK_INT >= 33) {
            val act = activity ?: return false
            return ContextCompat.checkSelfPermission(
                act,
                Manifest.permission.POST_NOTIFICATIONS
            ) == PackageManager.PERMISSION_GRANTED
        }
        return true
    }

    fun requestPermission() {
        if (Build.VERSION.SDK_INT >= 33) {
            val act = activity
            if (act != null && !hasPermission()) {
                ActivityCompat.requestPermissions(act, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
            }
        }
        plugin.emitGodotSignal("messaging_permission_result", hasPermission())
    }

    fun getPermissionStatus(): String {
        return if (hasPermission()) "authorized" else "denied"
    }
}
