package org.godotengine.plugin.firebase

import android.Manifest
import android.app.Activity
import android.content.Intent
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
        var instance: Messaging? = null
    }

    init {
        instance = this
    }

    private var activity: Activity? = null

    fun init(activity: Activity) {
        this.activity = activity
    }

    fun onMainResume() {
        checkIntent(activity?.intent)
        drainQueue()
    }

    private fun checkIntent(intent: Intent?) {
        val extras = intent?.extras
        if (extras != null && extras.containsKey("google.message_id")) {
            val payload = Dictionary()
            for (key in extras.keySet()) {
                val value = extras.get(key)
                if (value != null) {
                    payload[key] = value.toString()
                }
            }
            plugin.emitGodotSignal("messaging_notification_opened", payload)
            // Clear the extras to avoid re-emitting on next resume
            intent.removeExtra("google.message_id")
        }
    }
    
    fun drainQueue() {
        val events = MessagingEventQueue.drain()
        if (events.isEmpty()) return
        
        val currentActivity = activity ?: return
        currentActivity.runOnUiThread {
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
    }

    fun messagingSignals(): MutableSet<SignalInfo> {
        val signals: MutableSet<SignalInfo> = mutableSetOf()
        signals.add(SignalInfo("messaging_token_received", String::class.java))
        signals.add(SignalInfo("messaging_token_error", String::class.java))
        signals.add(SignalInfo("messaging_notification_received", Dictionary::class.java))
        signals.add(SignalInfo("messaging_notification_opened", Dictionary::class.java))
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
        val currentActivity = activity
        if (currentActivity == null) {
            plugin.emitGodotSignal("messaging_token_error", "Android activity is null")
            return
        }
        currentActivity.runOnUiThread {
            FirebaseMessaging.getInstance().token.addOnCompleteListener(currentActivity) { task ->
                if (!task.isSuccessful) {
                    val errorMsg = task.exception?.message ?: "Unknown error"
                    Log.w(TAG, "Fetching FCM registration token failed: $errorMsg", task.exception)
                    plugin.emitGodotSignal("messaging_token_error", errorMsg)
                    return@addOnCompleteListener
                }

                val token = task.result
                if (token != null) {
                    plugin.emitGodotSignal("messaging_token_received", token)
                } else {
                    plugin.emitGodotSignal("messaging_token_error", "Token is null")
                }
            }
        }
    }

    fun deleteToken() {
        val currentActivity = activity
        if (currentActivity == null) {
            plugin.emitGodotSignal("messaging_token_delete_failure", "Android activity is null")
            return
        }
        currentActivity.runOnUiThread {
            FirebaseMessaging.getInstance().deleteToken().addOnCompleteListener(currentActivity) { task ->
                if (task.isSuccessful) {
                    plugin.emitGodotSignal("messaging_token_delete_success")
                } else {
                    val errorMsg = task.exception?.message ?: "Unknown error"
                    Log.w(TAG, "Token deletion failed: $errorMsg", task.exception)
                    plugin.emitGodotSignal("messaging_token_delete_failure", errorMsg)
                }
            }
        }
    }

    fun subscribeToTopic(topic: String) {
        val currentActivity = activity
        if (currentActivity == null) {
            plugin.emitGodotSignal("messaging_topic_subscribe_failure", "Android activity is null")
            return
        }
        currentActivity.runOnUiThread {
            FirebaseMessaging.getInstance().subscribeToTopic(topic).addOnCompleteListener(currentActivity) { task ->
                if (task.isSuccessful) {
                    plugin.emitGodotSignal("messaging_topic_subscribe_success", topic)
                } else {
                    val errorMsg = task.exception?.message ?: "Unknown error"
                    Log.w(TAG, "Subscription to topic failed: $errorMsg", task.exception)
                    plugin.emitGodotSignal("messaging_topic_subscribe_failure", errorMsg)
                }
            }
        }
    }

    fun unsubscribeFromTopic(topic: String) {
        val currentActivity = activity
        if (currentActivity == null) {
            plugin.emitGodotSignal("messaging_topic_unsubscribe_failure", "Android activity is null")
            return
        }
        currentActivity.runOnUiThread {
            FirebaseMessaging.getInstance().unsubscribeFromTopic(topic).addOnCompleteListener(currentActivity) { task ->
                if (task.isSuccessful) {
                    plugin.emitGodotSignal("messaging_topic_unsubscribe_success", topic)
                } else {
                    val errorMsg = task.exception?.message ?: "Unknown error"
                    Log.w(TAG, "Unsubscription from topic failed: $errorMsg", task.exception)
                    plugin.emitGodotSignal("messaging_topic_unsubscribe_failure", errorMsg)
                }
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
        val currentActivity = activity
        if (currentActivity == null) {
            plugin.emitGodotSignal("messaging_permission_result", hasPermission())
            return
        }
        currentActivity.runOnUiThread {
            if (Build.VERSION.SDK_INT >= 33) {
                if (!hasPermission()) {
                    ActivityCompat.requestPermissions(currentActivity, arrayOf(Manifest.permission.POST_NOTIFICATIONS), 1001)
                }
            }
            plugin.emitGodotSignal("messaging_permission_result", hasPermission())
        }
    }

    fun getPermissionStatus(): String {
        return if (hasPermission()) "authorized" else "denied"
    }
}
