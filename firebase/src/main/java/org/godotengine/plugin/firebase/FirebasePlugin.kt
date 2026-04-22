package org.godotengine.plugin.firebase

import android.app.Activity
import android.content.Intent
import android.view.View
import org.godotengine.godot.Dictionary
import org.godotengine.godot.Godot
import org.godotengine.godot.plugin.GodotPlugin
import org.godotengine.godot.plugin.SignalInfo
import org.godotengine.godot.plugin.UsedByGodot

class FirebasePlugin(godot: Godot) : GodotPlugin(godot) {
	override fun getPluginName(): String = "GodotFirebaseAndroid"

	private val auth = Authentication(this)
	private val firestore = Firestore(this)
	private val storage = CloudStorage(this)
	private val realtimeDatabase = RealtimeDatabase(this)
	private val analytics = Analytics(this)
	private val remoteConfig = RemoteConfig(this)

	override fun onMainCreate(activity: Activity?): View? {
		activity?.let {
			auth.init(it)
			analytics.init(it)
		}
		return super.onMainCreate(activity)
	}

	override fun onMainActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		auth.handleActivityResult(requestCode, resultCode, data)
	}

	override fun getPluginSignals(): MutableSet<SignalInfo> {
		val signals: MutableSet<SignalInfo> = mutableSetOf()
		signals.addAll(auth.authSignals())
		signals.addAll(firestore.firestoreSignals())
		signals.addAll(realtimeDatabase.realtimeDbSignals())
		signals.addAll(storage.storageSignals())
		signals.addAll(analytics.analyticsSignals())
		signals.addAll(remoteConfig.remoteConfigSignals())
		return signals
	}

	fun emitGodotSignal(signalName: String, arg1: Any?, arg2: Any? = null) {
		if (arg2 != null) {
			emitSignal(signalName, arg1, arg2)
		} else {
			emitSignal(signalName, arg1)
		}
	}

	/**
	 * Authentication
	 */
	@UsedByGodot
	fun signInAnonymously() = auth.signInAnonymously()

	@UsedByGodot
	fun createUserWithEmailPassword(email: String, password: String) = auth.createUserWithEmailPassword(email, password)

	@UsedByGodot
	fun signInWithEmailPassword(email: String, password: String) = auth.signInWithEmailPassword(email, password)

	@UsedByGodot
	fun sendEmailVerification() = auth.sendEmailVerification()

	@UsedByGodot
	fun sendPasswordResetEmail(email: String) = auth.sendPasswordResetEmail(email)

	@UsedByGodot
	fun signInWithGoogle() = auth.signInWithGoogle()

	@UsedByGodot
	fun linkAnonymousWithGoogle() = auth.linkAnonymousWithGoogle()

	@UsedByGodot
	fun getCurrentUser() = auth.getCurrentUser()

	@UsedByGodot
	fun isSignedIn() = auth.isSignedIn()

	@UsedByGodot
	fun signOut() = auth.signOut()

	@UsedByGodot
	fun deleteUser() = auth.deleteUser()

	@UsedByGodot
	fun useAuthEmulator(host: String, port: Int) = auth.useEmulator(host, port)

	@UsedByGodot
	fun reauthenticateWithEmail(email: String, password: String) = auth.reauthenticateWithEmail(email, password)

	@UsedByGodot
	fun addAuthStateListener() = auth.addAuthStateListener()

	@UsedByGodot
	fun removeAuthStateListener() = auth.removeAuthStateListener()

	@UsedByGodot
	fun getIdToken(forceRefresh: Boolean) = auth.getIdToken(forceRefresh)

	@UsedByGodot
	fun updateProfile(displayName: String, photoUrl: String) = auth.updateProfile(displayName, photoUrl)

	@UsedByGodot
	fun updatePassword(newPassword: String) = auth.updatePassword(newPassword)

	@UsedByGodot
	fun reloadUser() = auth.reloadUser()

	@UsedByGodot
	fun unlinkProvider(providerId: String) = auth.unlinkProvider(providerId)

	/**
	 * Firestore
	 */

	@UsedByGodot
	fun firestoreAddDocument(collection: String, data: Dictionary) = firestore.addDocument(collection, data)

	@UsedByGodot
	fun firestoreSetDocument(collection: String, documentId: String, data: Dictionary, merge: Boolean = false) = firestore.setDocument(collection, documentId, data, merge)

	@UsedByGodot
	fun firestoreGetDocument(collection: String, documentId: String) = firestore.getDocument(collection, documentId)

	@UsedByGodot
	fun firestoreUpdateDocument(collection: String, documentId: String, data: Dictionary) = firestore.updateDocument(collection, documentId, data)

	@UsedByGodot
	fun firestoreDeleteDocument(collection: String, documentId: String) = firestore.deleteDocument(collection, documentId)

	@UsedByGodot
	fun firestoreGetDocumentsInCollection(collection: String) = firestore.getDocumentsInCollection(collection)

	@UsedByGodot
	fun firestoreQueryDocuments(collection: String, filtersJson: String, orderBy: String, orderDescending: Boolean, limitCount: Int) = firestore.queryDocuments(collection, filtersJson, orderBy, orderDescending, limitCount)

	@UsedByGodot
	fun firestoreListenToDocument(documentPath: String) = firestore.listenToDocument(documentPath)

	@UsedByGodot
	fun firestoreStopListeningToDocument(documentPath: String) = firestore.stopListeningToDocument(documentPath)

	@UsedByGodot
	fun firestoreUseEmulator(host: String, port: Int) = firestore.useEmulator(host, port)

	@UsedByGodot
	fun firestoreListenToCollection(collection: String) = firestore.listenToCollection(collection)

	@UsedByGodot
	fun firestoreStopListeningToCollection(collection: String) = firestore.stopListeningToCollection(collection)

	@UsedByGodot
	fun firestoreCreateBatch() = firestore.createBatch()

	@UsedByGodot
	fun firestoreBatchSet(batchId: Int, collection: String, documentId: String, data: Dictionary, merge: Boolean) = firestore.batchSet(batchId, collection, documentId, data, merge)

	@UsedByGodot
	fun firestoreBatchUpdate(batchId: Int, collection: String, documentId: String, data: Dictionary) = firestore.batchUpdate(batchId, collection, documentId, data)

	@UsedByGodot
	fun firestoreBatchDelete(batchId: Int, collection: String, documentId: String) = firestore.batchDelete(batchId, collection, documentId)

	@UsedByGodot
	fun firestoreCommitBatch(batchId: Int) = firestore.commitBatch(batchId)

	@UsedByGodot
	fun firestoreRunTransaction(collection: String, documentId: String, updateData: Dictionary) = firestore.runTransaction(collection, documentId, updateData)

	@UsedByGodot
	fun firestoreServerTimestamp() = firestore.serverTimestamp()

	@UsedByGodot
	fun firestoreArrayUnion(elementsJson: String) = firestore.arrayUnion(elementsJson)

	@UsedByGodot
	fun firestoreArrayRemove(elementsJson: String) = firestore.arrayRemove(elementsJson)

	@UsedByGodot
	fun firestoreIncrementBy(value: Double) = firestore.incrementBy(value)

	@UsedByGodot
	fun firestoreDeleteField() = firestore.deleteField()

	/**
	 * Cloud Storage
	 */

	@UsedByGodot
	fun storageUploadFile(path: String, localFilePath: String) = storage.uploadFile(path, localFilePath)

	@UsedByGodot
	fun storageGetDownloadUrl(path: String) = storage.getDownloadUrl(path)

	@UsedByGodot
	fun storageDownloadFile(path: String, destinationPath: String) = storage.downloadFile(path, destinationPath)

	@UsedByGodot
	fun storageGetMetadata(path: String) = storage.getMetadata(path)

	@UsedByGodot
	fun storageDeleteFile(path: String) = storage.deleteFile(path)

	@UsedByGodot
	fun storageListFiles(path: String) = storage.listFiles(path)

	/**
	 * Realtime Database
	 */

	@UsedByGodot
	fun rtdbSetValue(path: String, data: Dictionary) = realtimeDatabase.setValue(path, data)

	@UsedByGodot
	fun rtdbGetValue(path: String) = realtimeDatabase.getValue(path)

	@UsedByGodot
	fun rtdbUpdateValue(path: String, data: Dictionary) = realtimeDatabase.updateValue(path, data)

	@UsedByGodot
	fun rtdbDeleteValue(path: String) = realtimeDatabase.deleteValue(path)

	@UsedByGodot
	fun rtdbListenToPath(path: String) = realtimeDatabase.listenToPath(path)

	@UsedByGodot
	fun rtdbStopListening(path: String) = realtimeDatabase.stopListening(path)

	/**
	 * Analytics
	 */

	@UsedByGodot
	fun analyticsLogEvent(name: String, parameters: Dictionary) = analytics.logEvent(name, parameters)

	@UsedByGodot
	fun analyticsSetUserProperty(name: String, value: String) = analytics.setUserProperty(name, value)

	@UsedByGodot
	fun analyticsSetUserId(id: String) = analytics.setUserId(id)

	@UsedByGodot
	fun analyticsSetAnalyticsCollectionEnabled(enabled: Boolean) = analytics.setAnalyticsCollectionEnabled(enabled)

	@UsedByGodot
	fun analyticsResetAnalyticsData() = analytics.resetAnalyticsData()

	@UsedByGodot
	fun analyticsSetDefaultEventParameters(parameters: Dictionary) = analytics.setDefaultEventParameters(parameters)

	@UsedByGodot
	fun analyticsGetAppInstanceId() = analytics.getAppInstanceId()

	@UsedByGodot
	fun analyticsSetConsent(adStorage: Boolean, analyticsStorage: Boolean, adUserData: Boolean, adPersonalization: Boolean) = analytics.setConsent(adStorage, analyticsStorage, adUserData, adPersonalization)

	@UsedByGodot
	fun analyticsSetSessionTimeout(seconds: Int) = analytics.setSessionTimeout(seconds)

	/**
	 * Remote Config
	 */

	@UsedByGodot
	fun remoteConfigInitialize() = remoteConfig.initialize()

	@UsedByGodot
	fun remoteConfigSetDefaults(defaults: Dictionary) = remoteConfig.setDefaults(defaults)

	@UsedByGodot
	fun remoteConfigSetMinimumFetchInterval(seconds: Long) = remoteConfig.setMinimumFetchInterval(seconds)

	@UsedByGodot
	fun remoteConfigSetFetchTimeout(seconds: Long) = remoteConfig.setFetchTimeout(seconds)

	@UsedByGodot
	fun remoteConfigFetch() = remoteConfig.fetch()

	@UsedByGodot
	fun remoteConfigActivate() = remoteConfig.activate()

	@UsedByGodot
	fun remoteConfigFetchAndActivate() = remoteConfig.fetchAndActivate()

	@UsedByGodot
	fun remoteConfigGetString(key: String) = remoteConfig.getString(key)

	@UsedByGodot
	fun remoteConfigGetBoolean(key: String) = remoteConfig.getBoolean(key)

	@UsedByGodot
	fun remoteConfigGetLong(key: String) = remoteConfig.getLong(key)

	@UsedByGodot
	fun remoteConfigGetDouble(key: String) = remoteConfig.getDouble(key)

	@UsedByGodot
	fun remoteConfigGetAll() = remoteConfig.getAll()

	@UsedByGodot
	fun remoteConfigGetJson(key: String) = remoteConfig.getJson(key)

	@UsedByGodot
	fun remoteConfigGetValueSource(key: String) = remoteConfig.getValueSource(key)

	@UsedByGodot
	fun remoteConfigGetLastFetchStatus() = remoteConfig.getLastFetchStatus()

	@UsedByGodot
	fun remoteConfigGetLastFetchTime() = remoteConfig.getLastFetchTime()

	@UsedByGodot
	fun remoteConfigListenForUpdates() = remoteConfig.listenForUpdates()

	@UsedByGodot
	fun remoteConfigStopListeningForUpdates() = remoteConfig.stopListeningForUpdates()
}
