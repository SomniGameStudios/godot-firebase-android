---
layout: default
title: Authentication
nav_order: 2
---

# Authentication

The Firebase Authentication module in **GodotFirebaseAndroid** supports anonymous login, email/password login, Google login, and account management. Each method emits signals to indicate success or failure.

## Signals

- `auth_success(current_user_data: Dictionary)`
  Emitted when a user successfully signs in. The dictionary contains user information such as UID, email, etc.

- `auth_failure(error_message: String)`
  Emitted when an authentication operation fails.

- `link_with_google_success(current_user_data: Dictionary)`
  Emitted when an anonymous user is successfully linked to a Google account.

- `link_with_google_failure(error_message: String)`
  Emitted when linking an anonymous user to a Google account fails.

- `sign_out_success(success: bool)`
  Emitted after a sign-out operation. `true` indicates success.

- `password_reset_sent(success: bool)`
  Emitted after attempting to send a password reset email.

- `email_verification_sent(success: bool)`
  Emitted after attempting to send an email verification email.

- `user_deleted(success: bool)`
  Emitted after an attempt to delete the current user.

- `auth_state_changed(signed_in: bool, current_user_data: Dictionary)`
  Emitted when the user's authentication state changes (sign in or sign out). Fires immediately when the listener is added with the current state.

- `id_token_result(token: String)`
  Emitted when an ID token is successfully retrieved.

- `id_token_error(error_message: String)`
  Emitted when retrieving an ID token fails.

- `profile_updated(success: bool)`
  Emitted when a user profile update succeeds.

- `profile_update_failure(error_message: String)`
  Emitted when a user profile update fails.

## Methods

{: .text-green-100 }
### sign_in_anonymously()

Signs in the user anonymously.

**Emits:** `auth_success` or `auth_failure`.

```gdscript
Firebase.auth.sign_in_anonymously()
```

---

{: .text-green-100 }
### create_user_with_email_password(email: String, password: String)

Creates a new user with the given email and password.

**Emits:** `auth_success` or `auth_failure`.

```gdscript
Firebase.auth.create_user_with_email_password("testuser@email.com", "password123")
```

---

{: .text-green-100 }
### sign_in_with_email_password(email: String, password: String)

Signs in a user using email and password credentials.

**Emits:** `auth_success` or `auth_failure`.

```gdscript
Firebase.auth.sign_in_with_email_password("testuser@email.com", "password123")
```
---

{: .text-green-100 }
### send_password_reset_email(email: String)

Sends a password reset email to the specified address.

**Emits:** `password_reset_sent`. It also emits `auth_failure` on failure.

```gdscript
Firebase.auth.send_password_reset_email("testuser@email.com")
```
---

{: .text-green-100 }
### send_email_verification()

Sends an email verification to the currently signed-in user.

**Emits:** `email_verification_sent`. It also emits `auth_failure` on failure.

```gdscript
Firebase.auth.send_email_verification()
```
---

{: .text-green-100 }
### sign_in_with_google()

Signs in the user using Google. Ensure Google Sign-In is properly configured in the Firebase console.

**Emits:** `auth_success` or `auth_failure`.

```gdscript
Firebase.auth.sign_in_with_google()
```
---

{: .text-green-100 }
### link_anonymous_with_google()

Links the currently signed-in anonymous user to a Google account, converting it to a permanent account. The anonymous user's UID and data are preserved. Must be called while an anonymous user is signed in.

**Emits:** `link_with_google_success` or `link_with_google_failure`.

```gdscript
Firebase.auth.link_anonymous_with_google()
```

---

{: .text-green-100 }
### get_current_user_data() -> Dictionary

If no user is signed in, returns a dictionary with error.

**Returns** a dictionary containing the currently signed-in user's data:
- `name` — Display name
- `email` — Email address
- `photoUrl` — Profile photo URL
- `emailVerified` — Whether the email is verified
- `isAnonymous` — Whether the user is anonymous
- `uid` — User ID

```gdscript
Firebase.auth.get_current_user_data()
```
---

{: .text-green-100 }
### delete_current_user()

Deletes the currently signed-in user.

**Emits:** `user_deleted`. It also emits `auth_failure` on failure.

```gdscript
Firebase.auth.delete_current_user()
```
---

{: .text-green-100 }
### is_signed_in() -> bool

**Returns** `true` if a user is currently signed in, otherwise `false`.

```gdscript
Firebase.auth.is_signed_in()
```
---

{: .text-green-100 }
### sign_out()

Signs out the current user.

**Emits:** `sign_out_success`. It also emits `auth_failure` on failure.

```gdscript
Firebase.auth.sign_out()
```

---

{: .text-green-100 }
### use_emulator(host: String, port: int)

Connects the Auth module to a local Firebase Auth emulator. Must be called before any other Auth operations.

{: .warning }
Only use this during development. Do not ship with emulator enabled.

```gdscript
Firebase.auth.use_emulator("10.0.2.2", 9099)
```

---

{: .text-green-100 }
### reauthenticate_with_email(email: String, password: String)

Reauthenticates the current user with email and password credentials. Required before sensitive operations like `update_password` or `delete_current_user` if the user's last sign-in was too long ago.

**Emits:** `auth_success` or `auth_failure`.

```gdscript
Firebase.auth.reauthenticate_with_email("testuser@email.com", "password123")
```

---

{: .text-green-100 }
### add_auth_state_listener()

Starts listening for authentication state changes. The listener fires immediately with the current state, then again whenever the user signs in or out.

**Emits:** `auth_state_changed` on every state change.

```gdscript
Firebase.auth.add_auth_state_listener()
```

---

{: .text-green-100 }
### remove_auth_state_listener()

Stops listening for authentication state changes.

```gdscript
Firebase.auth.remove_auth_state_listener()
```

---

{: .text-green-100 }
### get_id_token(force_refresh: bool = false)

Retrieves the Firebase ID token for the current user. The token can be used to authenticate with your backend server. Set `force_refresh` to `true` to force a token refresh even if the current token hasn't expired.

**Emits:** `id_token_result` or `id_token_error`.

```gdscript
Firebase.auth.get_id_token()       # use cached token
Firebase.auth.get_id_token(true)   # force refresh
```

---

{: .text-green-100 }
### update_profile(display_name: String, photo_url: String = "")

Updates the current user's display name and/or photo URL. Pass an empty string to leave a field unchanged.

**Emits:** `profile_updated` or `profile_update_failure`.

```gdscript
Firebase.auth.update_profile("Alice", "https://example.com/photo.png")
Firebase.auth.update_profile("Alice")  # update name only
```

---

{: .text-green-100 }
### update_password(new_password: String)

Updates the current user's password. The user must have been recently authenticated (see `reauthenticate_with_email`).

**Emits:** `auth_success` or `auth_failure`.

```gdscript
Firebase.auth.update_password("newSecurePassword123")
```

---

{: .text-green-100 }
### reload_user()

Reloads the current user's profile data from Firebase. Useful after operations like email verification to get the updated `emailVerified` status.

**Emits:** `auth_success` or `auth_failure`.

```gdscript
Firebase.auth.reload_user()
```

---

{: .text-green-100 }
### unlink_provider(provider_id: String)

Unlinks a provider from the current user's account. Common provider IDs: `"google.com"`, `"password"`.

**Emits:** `auth_success` or `auth_failure`.

```gdscript
Firebase.auth.unlink_provider("google.com")
```
