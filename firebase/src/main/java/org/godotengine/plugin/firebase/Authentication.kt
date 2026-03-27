package org.godotengine.plugin.firebase

import android.app.Activity
import android.content.Intent
import android.net.Uri
import android.util.Log
import com.google.android.gms.auth.api.signin.GoogleSignIn
import com.google.android.gms.auth.api.signin.GoogleSignInClient
import com.google.android.gms.auth.api.signin.GoogleSignInOptions
import com.google.android.gms.common.api.ApiException
import com.google.firebase.auth.EmailAuthProvider
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.auth.GoogleAuthProvider
import com.google.firebase.auth.UserProfileChangeRequest
import com.google.firebase.auth.auth
import com.google.firebase.Firebase

import org.godotengine.godot.Dictionary
import org.godotengine.godot.plugin.SignalInfo

class Authentication(private val plugin: FirebasePlugin) {
	companion object {
		private const val GOOGLE_SIGN_IN = 9001
		private const val TAG = "GodotFirebaseAuth"
	}

	private lateinit var activity: android.app.Activity
	private val auth: FirebaseAuth = Firebase.auth
	private lateinit var googleSignInClient: GoogleSignInClient
	private var isLinkingAnonymous = false
	private var authStateListener: FirebaseAuth.AuthStateListener? = null

	fun authSignals(): MutableSet<SignalInfo> {
		val signals: MutableSet<SignalInfo> = mutableSetOf()
		signals.add(SignalInfo("auth_success", Dictionary::class.java))
		signals.add(SignalInfo("auth_failure", String::class.java))
		signals.add(SignalInfo("link_with_google_success", Dictionary::class.java))
		signals.add(SignalInfo("link_with_google_failure", String::class.java))
		signals.add(SignalInfo("sign_out_success", Boolean::class.javaObjectType))
		signals.add(SignalInfo("password_reset_sent", Boolean::class.javaObjectType))
		signals.add(SignalInfo("email_verification_sent", Boolean::class.javaObjectType))
		signals.add(SignalInfo("user_deleted", Boolean::class.javaObjectType))
		signals.add(SignalInfo("auth_state_changed", Boolean::class.javaObjectType, Dictionary::class.java))
		signals.add(SignalInfo("id_token_result", String::class.java))
		signals.add(SignalInfo("id_token_error", String::class.java))
		signals.add(SignalInfo("profile_updated", Boolean::class.javaObjectType))
		signals.add(SignalInfo("profile_update_failure", String::class.java))
		return signals
	}

	fun init(activity: Activity) {
		this.activity = activity
		val resId = activity.resources.getIdentifier("default_web_client_id", "string", activity.packageName)

		if (resId == 0) {
			Log.e(TAG, "default_web_client_id not found in app resources.")
			return
		}

		val webClientId = activity.getString(resId)

		val gso = GoogleSignInOptions.Builder(GoogleSignInOptions.DEFAULT_SIGN_IN)
			.requestIdToken(webClientId)
			.requestEmail()
			.build()

		googleSignInClient = GoogleSignIn.getClient(activity, gso)
	}

	fun handleActivityResult(requestCode: Int, resultCode: Int, data: Intent?) {
		if (requestCode == GOOGLE_SIGN_IN) {
			val task = GoogleSignIn.getSignedInAccountFromIntent(data)
			try {
				val account = task.getResult(ApiException::class.java)!!
				Log.d(TAG, "authWithGoogle:" + account.id)
				if (isLinkingAnonymous) {
					isLinkingAnonymous = false
					linkWithGoogle(account.idToken!!)
				} else {
					authWithGoogle(account.idToken!!)
				}
			} catch (e: ApiException) {
				val wasLinking = isLinkingAnonymous
				isLinkingAnonymous = false
				Log.w(TAG, "Google sign in failed", e)
				if (wasLinking) {
					plugin.emitGodotSignal("link_with_google_failure", e.message ?: "Unknown error")
				} else {
					plugin.emitGodotSignal("auth_failure", e.message ?: "Unknown error")
				}
			}
		}
	}

	fun signInAnonymously() {
		val currentUser = auth.currentUser
		if (currentUser != null) {
			Log.d(TAG, "User already signed in (uid=${currentUser.uid}, isAnonymous=${currentUser.isAnonymous}). Skipping anonymous sign-in.")
			plugin.emitGodotSignal("auth_failure", "User is already signed in.")
			return
		}
		auth.signInAnonymously()
			.addOnSuccessListener {
				val uid = it.user?.uid
				Log.d(TAG, "Signed in anonymously as $uid")
				plugin.emitGodotSignal("auth_success", getCurrentUser())
			}
			.addOnFailureListener { e ->
				Log.d(TAG, "Anonymous sign-in failed", e)
				plugin.emitGodotSignal("auth_failure", e.message ?: "Unknown error")
			}
	}

	fun createUserWithEmailPassword(email: String, password: String) {
		auth.createUserWithEmailAndPassword(email, password)
			.addOnSuccessListener {
				Log.d(TAG, "User created with email: $email")
				plugin.emitGodotSignal("auth_success", getCurrentUser())
			}
			.addOnFailureListener { e ->
				Log.d(TAG, "User creation failed", e)
				plugin.emitGodotSignal("auth_failure", e.message ?: "Unknown error")
			}
	}

	fun signInWithEmailPassword(email: String, password: String) {
		auth.signInWithEmailAndPassword(email, password)
			.addOnSuccessListener {
				Log.d(TAG, "Signed in with email: $email")
				plugin.emitGodotSignal("auth_success", getCurrentUser())
			}
			.addOnFailureListener { e ->
				Log.d(TAG, "Sign-in with email failed", e)
				plugin.emitGodotSignal("auth_failure", e.message ?: "Unknown error")
			}
	}

	fun sendEmailVerification() {
		auth.currentUser?.sendEmailVerification()
			?.addOnSuccessListener {
				Log.d(TAG, "Verification email sent.")
				plugin.emitGodotSignal("email_verification_sent", true)
			}
			?.addOnFailureListener { e ->
				Log.e(TAG, "Failed to send verification email", e)
				plugin.emitGodotSignal("email_verification_sent", false)
				plugin.emitGodotSignal("auth_failure", "Failed to send verification email: ${e.message}")
			}
	}

	fun sendPasswordResetEmail(email: String) {
		auth.sendPasswordResetEmail(email)
			.addOnSuccessListener {
				Log.d(TAG, "Password reset email sent to $email.")
				plugin.emitGodotSignal("password_reset_sent", true)
			}
			.addOnFailureListener { e ->
				Log.e(TAG, "Password reset failed", e)
				plugin.emitGodotSignal("password_reset_sent", false)
				plugin.emitGodotSignal("auth_failure", "Failed to send verification email: ${e.message}")
			}
	}

	fun signInWithGoogle() {
		if (!::googleSignInClient.isInitialized) {
			Log.e(TAG, "GoogleSignInClient not initialized.")
			plugin.emitGodotSignal("auth_failure", "Google Sign-In not initialized.")
			return
		}
		try {
			val signInIntent = googleSignInClient.signInIntent
			activity.startActivityForResult(signInIntent, GOOGLE_SIGN_IN)
		} catch (e: Exception) {
			Log.e(TAG, "Error starting Google Sign-In", e)
		}
	}

	fun linkAnonymousWithGoogle() {
		val currentUser = auth.currentUser
		if (currentUser == null) {
			Log.e(TAG, "No user signed in.")
			plugin.emitGodotSignal("link_with_google_failure", "No user signed in.")
			return
		}
		if (!currentUser.isAnonymous) {
			Log.d(TAG, "Current user is not anonymous (uid=${currentUser.uid}). Cannot link.")
			plugin.emitGodotSignal("link_with_google_failure", "Current user is not anonymous.")
			return
		}
		Log.d(TAG, "Linking anonymous user (uid=${currentUser.uid}) with Google.")
		isLinkingAnonymous = true
		signInWithGoogle()
	}

	private fun authWithGoogle(idToken: String) {
		val credential = GoogleAuthProvider.getCredential(idToken, null)
		auth.signInWithCredential(credential)
			.addOnSuccessListener { authResult ->
				val uid = authResult.user?.uid
				Log.d(TAG, "signInWithCredential:success -> $uid")
				plugin.emitGodotSignal("auth_success", getCurrentUser())
			}
			.addOnFailureListener { e ->
				Log.w(TAG, "signInWithCredential:failure", e)
				plugin.emitGodotSignal("auth_failure", e.message ?: "Unknown error")
			}
	}

	private fun linkWithGoogle(idToken: String) {
		val currentUser = auth.currentUser
		if (currentUser == null) {
			Log.e(TAG, "No user signed in during linkWithGoogle.")
			plugin.emitGodotSignal("link_with_google_failure", "No user signed in.")
			return
		}
		val credential = GoogleAuthProvider.getCredential(idToken, null)
		currentUser.linkWithCredential(credential)
			.addOnSuccessListener { authResult ->
				val uid = authResult.user?.uid
				Log.d(TAG, "linkWithCredential:success -> $uid")
				plugin.emitGodotSignal("link_with_google_success", getCurrentUser())
			}
			.addOnFailureListener { e ->
				Log.w(TAG, "linkWithCredential:failure", e)
				plugin.emitGodotSignal("link_with_google_failure", e.message ?: "Unknown error")
			}
	}

	fun getCurrentUser(): Dictionary {
		val user = auth.currentUser
		val userData = Dictionary()
		if (user != null) {
			userData["name"] = user.displayName
			userData["email"] = user.email
			userData["photoUrl"] = user.photoUrl?.toString()
			userData["emailVerified"] = user.isEmailVerified
			userData["isAnonymous"] = user.isAnonymous
			userData["uid"] = user.uid
			userData["hasGoogle"] = user.providerData.any { it.providerId == "google.com" }
			userData["hasApple"] = user.providerData.any { it.providerId == "apple.com" }
			userData["providerIds"] = user.providerData.joinToString(",") { it.providerId }
		} else {
			userData["error"] = "No user signed in"
			Log.d(TAG, "No user signed in")
		}

		return userData
	}

	fun isSignedIn(): Boolean {
		return auth.currentUser != null
	}

	fun signOut() {
		auth.signOut()
		googleSignInClient.signOut()
			.addOnCompleteListener(activity) {
				plugin.emitGodotSignal("sign_out_success", true)
			}
			.addOnFailureListener { e ->
				Log.d(TAG, "Sign out failed", e)
				plugin.emitGodotSignal("sign_out_success", false)
				plugin.emitGodotSignal("auth_failure", "Failed to sign out: ${e.message}")
			}
	}

	fun deleteUser() {
		auth.currentUser?.delete()
			?.addOnSuccessListener {
				Log.d(TAG, "User deleted.")
				plugin.emitGodotSignal("user_deleted", true)
			}
			?.addOnFailureListener { e ->
				Log.e(TAG, "Failed to delete user", e)
				plugin.emitGodotSignal("user_deleted", false)
				plugin.emitGodotSignal("auth_failure", "Delete failed: ${e.message}")
			}
	}

	fun useEmulator(host: String, port: Int) {
		auth.useEmulator(host, port)
		Log.d(TAG, "Using Auth emulator at $host:$port")
	}

	fun reauthenticateWithEmail(email: String, password: String) {
		val user = auth.currentUser
		if (user == null) {
			plugin.emitGodotSignal("auth_failure", "No user signed in.")
			return
		}
		val credential = EmailAuthProvider.getCredential(email, password)
		user.reauthenticate(credential)
			.addOnSuccessListener {
				Log.d(TAG, "Reauthentication successful.")
				plugin.emitGodotSignal("auth_success", getCurrentUser())
			}
			.addOnFailureListener { e ->
				Log.e(TAG, "Reauthentication failed", e)
				plugin.emitGodotSignal("auth_failure", e.message ?: "Reauthentication failed")
			}
	}

	fun addAuthStateListener() {
		if (authStateListener != null) return
		authStateListener = FirebaseAuth.AuthStateListener { firebaseAuth ->
			val user = firebaseAuth.currentUser
			val signedIn = user != null
			plugin.emitGodotSignal("auth_state_changed", signedIn, getCurrentUser())
		}
		auth.addAuthStateListener(authStateListener!!)
		Log.d(TAG, "Auth state listener added.")
	}

	fun removeAuthStateListener() {
		authStateListener?.let {
			auth.removeAuthStateListener(it)
			authStateListener = null
			Log.d(TAG, "Auth state listener removed.")
		}
	}

	fun getIdToken(forceRefresh: Boolean) {
		val user = auth.currentUser
		if (user == null) {
			plugin.emitGodotSignal("id_token_error", "No user signed in.")
			return
		}
		user.getIdToken(forceRefresh)
			.addOnSuccessListener { result ->
				val token = result.token ?: ""
				Log.d(TAG, "ID token retrieved.")
				plugin.emitGodotSignal("id_token_result", token)
			}
			.addOnFailureListener { e ->
				Log.e(TAG, "Failed to get ID token", e)
				plugin.emitGodotSignal("id_token_error", e.message ?: "Failed to get ID token")
			}
	}

	fun updateProfile(displayName: String, photoUrl: String) {
		val user = auth.currentUser
		if (user == null) {
			plugin.emitGodotSignal("profile_update_failure", "No user signed in.")
			return
		}
		val profileUpdates = UserProfileChangeRequest.Builder()
			.setDisplayName(displayName.ifEmpty { null })
			.setPhotoUri(if (photoUrl.isNotEmpty()) Uri.parse(photoUrl) else null)
			.build()
		user.updateProfile(profileUpdates)
			.addOnSuccessListener {
				Log.d(TAG, "Profile updated.")
				plugin.emitGodotSignal("profile_updated", true)
			}
			.addOnFailureListener { e ->
				Log.e(TAG, "Profile update failed", e)
				plugin.emitGodotSignal("profile_update_failure", e.message ?: "Profile update failed")
			}
	}

	fun updatePassword(newPassword: String) {
		val user = auth.currentUser
		if (user == null) {
			plugin.emitGodotSignal("auth_failure", "No user signed in.")
			return
		}
		user.updatePassword(newPassword)
			.addOnSuccessListener {
				Log.d(TAG, "Password updated.")
				plugin.emitGodotSignal("auth_success", getCurrentUser())
			}
			.addOnFailureListener { e ->
				Log.e(TAG, "Password update failed", e)
				plugin.emitGodotSignal("auth_failure", e.message ?: "Password update failed")
			}
	}

	fun reloadUser() {
		val user = auth.currentUser
		if (user == null) {
			plugin.emitGodotSignal("auth_failure", "No user signed in.")
			return
		}
		user.reload()
			.addOnSuccessListener {
				Log.d(TAG, "User reloaded.")
				plugin.emitGodotSignal("auth_success", getCurrentUser())
			}
			.addOnFailureListener { e ->
				Log.e(TAG, "User reload failed", e)
				plugin.emitGodotSignal("auth_failure", e.message ?: "User reload failed")
			}
	}

	fun unlinkProvider(providerId: String) {
		val user = auth.currentUser
		if (user == null) {
			plugin.emitGodotSignal("auth_failure", "No user signed in.")
			return
		}
		user.unlink(providerId)
			.addOnSuccessListener {
				Log.d(TAG, "Provider $providerId unlinked.")
				plugin.emitGodotSignal("auth_success", getCurrentUser())
			}
			.addOnFailureListener { e ->
				Log.e(TAG, "Unlink provider failed", e)
				plugin.emitGodotSignal("auth_failure", e.message ?: "Unlink failed")
			}
	}

}
