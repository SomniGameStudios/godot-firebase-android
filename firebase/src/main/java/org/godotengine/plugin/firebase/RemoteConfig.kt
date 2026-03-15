package org.godotengine.plugin.firebase

import android.util.Log
import com.google.firebase.remoteconfig.ConfigUpdateListenerRegistration
import com.google.firebase.remoteconfig.FirebaseRemoteConfig
import com.google.firebase.remoteconfig.FirebaseRemoteConfigSettings
import org.godotengine.godot.Dictionary
import org.godotengine.godot.plugin.SignalInfo
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class RemoteConfig(private val plugin: FirebasePlugin) {
	companion object {
		private const val TAG = "GodotFirebaseRemoteConfig"
	}

	private val remoteConfig: FirebaseRemoteConfig = FirebaseRemoteConfig.getInstance()
	private var minimumFetchIntervalSeconds: Long = 3600
	private var fetchTimeoutSeconds: Long = 60
	private var configUpdateListener: ConfigUpdateListenerRegistration? = null

	fun remoteConfigSignals(): MutableSet<SignalInfo> {
		val signals: MutableSet<SignalInfo> = mutableSetOf()
		signals.add(SignalInfo("remote_config_fetch_completed", Dictionary::class.java))
		signals.add(SignalInfo("remote_config_activate_completed", Dictionary::class.java))
		signals.add(SignalInfo("remote_config_updated", Array::class.java))
		return signals
	}

	fun initialize() {
		applyConfigSettings()
		Log.d(TAG, "Remote Config initialized")
	}

	fun setMinimumFetchInterval(seconds: Long) {
		minimumFetchIntervalSeconds = seconds
		applyConfigSettings()
		Log.d(TAG, "Minimum fetch interval set to $seconds seconds")
	}

	fun setFetchTimeout(seconds: Long) {
		fetchTimeoutSeconds = seconds
		applyConfigSettings()
		Log.d(TAG, "Fetch timeout set to $seconds seconds")
	}

	private fun applyConfigSettings() {
		val configSettings = FirebaseRemoteConfigSettings.Builder()
			.setMinimumFetchIntervalInSeconds(minimumFetchIntervalSeconds)
			.setFetchTimeoutInSeconds(fetchTimeoutSeconds)
			.build()
		remoteConfig.setConfigSettingsAsync(configSettings)
	}

	fun setDefaults(defaults: Dictionary) {
		val defaultsMap = mutableMapOf<String, Any>()
		for (key in defaults.keys) {
			val value = defaults[key]
			if (value != null) {
				defaultsMap[key.toString()] = value
			}
		}
		remoteConfig.setDefaultsAsync(defaultsMap)
		Log.d(TAG, "Defaults set: ${defaultsMap.keys}")
	}

	fun fetch() {
		remoteConfig.fetch()
			.addOnSuccessListener {
				val result = Dictionary()
				result["status"] = true
				result["error"] = ""
				Log.d(TAG, "Fetch succeeded")
				plugin.emitGodotSignal("remote_config_fetch_completed", result)
			}
			.addOnFailureListener { e ->
				val result = Dictionary()
				result["status"] = false
				result["error"] = e.message ?: "Unknown error"
				Log.e(TAG, "Fetch failed", e)
				plugin.emitGodotSignal("remote_config_fetch_completed", result)
			}
	}

	fun activate() {
		remoteConfig.activate()
			.addOnSuccessListener { activated ->
				val result = Dictionary()
				result["status"] = true
				result["activated"] = activated
				result["error"] = ""
				Log.d(TAG, "Activate succeeded (activated=$activated)")
				plugin.emitGodotSignal("remote_config_activate_completed", result)
			}
			.addOnFailureListener { e ->
				val result = Dictionary()
				result["status"] = false
				result["activated"] = false
				result["error"] = e.message ?: "Unknown error"
				Log.e(TAG, "Activate failed", e)
				plugin.emitGodotSignal("remote_config_activate_completed", result)
			}
	}

	fun fetchAndActivate() {
		remoteConfig.fetchAndActivate()
			.addOnSuccessListener { activated ->
				val result = Dictionary()
				result["status"] = true
				result["activated"] = activated
				result["error"] = ""
				Log.d(TAG, "Fetch and activate succeeded (activated=$activated)")
				plugin.emitGodotSignal("remote_config_fetch_completed", result)
			}
			.addOnFailureListener { e ->
				val result = Dictionary()
				result["status"] = false
				result["activated"] = false
				result["error"] = e.message ?: "Unknown error"
				Log.e(TAG, "Fetch and activate failed", e)
				plugin.emitGodotSignal("remote_config_fetch_completed", result)
			}
	}

	fun getString(key: String): String {
		return remoteConfig.getString(key)
	}

	fun getBoolean(key: String): Boolean {
		return remoteConfig.getBoolean(key)
	}

	fun getLong(key: String): Long {
		return remoteConfig.getLong(key)
	}

	fun getDouble(key: String): Double {
		return remoteConfig.getDouble(key)
	}

	fun getAll(): Dictionary {
		val result = Dictionary()
		for ((key, value) in remoteConfig.all) {
			result[key] = value.asString()
		}
		return result
	}

	fun getJson(key: String): String {
		return remoteConfig.getString(key)
	}

	fun getValueSource(key: String): Int {
		return remoteConfig.getValue(key).source
	}

	fun getLastFetchStatus(): Int {
		return remoteConfig.info.lastFetchStatus
	}

	fun getLastFetchTime(): String {
		val millis = remoteConfig.info.fetchTimeMillis
		if (millis <= 0) return ""
		val sdf = SimpleDateFormat("yyyy-MM-dd'T'HH:mm:ss'Z'", Locale.US)
		sdf.timeZone = TimeZone.getTimeZone("UTC")
		return sdf.format(Date(millis))
	}

	fun listenForUpdates() {
		if (configUpdateListener != null) return
		configUpdateListener = remoteConfig.addOnConfigUpdateListener { configUpdate, error ->
			if (error != null) {
				Log.e(TAG, "Config update listener error", error)
				return@addOnConfigUpdateListener
			}
			val updatedKeys = configUpdate?.updatedKeys?.toTypedArray() ?: emptyArray()
			Log.d(TAG, "Config updated, keys: ${updatedKeys.toList()}")
			plugin.emitGodotSignal("remote_config_updated", updatedKeys)
		}
		Log.d(TAG, "Config update listener added")
	}

	fun stopListeningForUpdates() {
		configUpdateListener?.remove()
		configUpdateListener = null
		Log.d(TAG, "Config update listener removed")
	}
}
