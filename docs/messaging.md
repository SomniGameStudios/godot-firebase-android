---
layout: default
title: Cloud Messaging (FCM)
nav_order: 3
---

# Firebase Cloud Messaging

Push notifications via Firebase Cloud Messaging (FCM) on Android.

## Setup

### 1. Project Configuration

Ensure your `google-services.json` is placed in:
`res://addons/GodotFirebaseAndroid/google-services.json`

The export plugin will automatically copy it to the Android build directory and apply the `com.google.gms.google-services` plugin.

### 2. Post-Notifications Permission (Android 13+)

Starting with Android 13 (API 33), you must request the `POST_NOTIFICATIONS` permission. The plugin handles the manifest declaration automatically, but you should still call `request_permission()` at runtime.

---

## Usage

### Initialize

```gdscript
func _ready() -> void:
    Firebase.messaging.token_received.connect(_on_token)
    Firebase.messaging.notification_received.connect(_on_notification)
    Firebase.messaging.notification_opened.connect(_on_notification_opened)
    Firebase.messaging.permission_result.connect(_on_permission)
    
    # Optional: not strictly required on Android as it's automatic, 
    # but kept for cross-platform API parity.
    Firebase.messaging.configure()
```

### Request Permission

```gdscript
Firebase.messaging.request_permission()
```

### Check Permission Status

```gdscript
var status := Firebase.messaging.get_permission_status()
# Returns: "authorized", "denied", or "unsupported" (below Android 13)
```

### Get FCM Token

```gdscript
Firebase.messaging.get_token()

func _on_token(token: String) -> void:
    print("FCM Token: ", token)
```

### Delete Token (Logout)

```gdscript
Firebase.messaging.delete_token()
```

### Topics

```gdscript
Firebase.messaging.subscribe_to_topic("all_users")
Firebase.messaging.unsubscribe_from_topic("all_users")
```

---

## Signals

| Signal | Arguments | Description |
|--------|-----------|-------------|
| `token_received` | `token: String` | FCM registration token (initial + refresh) |
| `token_error` | `message: String` | Failed to retrieve token |
| `notification_received` | `data: Dictionary` | Foreground notification payload |
| `notification_opened` | `data: Dictionary` | User tapped a notification |
| `permission_result` | `granted: bool` | Result of `request_permission()` |
| `topic_subscribe_success` | `topic: String` | Topic subscription succeeded |
| `topic_subscribe_failure` | `message: String` | Topic subscription failed |
| `topic_unsubscribe_success` | `topic: String` | Topic unsubscription succeeded |
| `topic_unsubscribe_failure` | `message: String` | Topic unsubscription failed |
| `token_delete_success` | — | Token deleted |
| `token_delete_failure` | `message: String` | Token deletion failed |

## Notification Payload

The `data` Dictionary from `notification_received` and `notification_opened` contains:

| Key | Description |
|-----|-------------|
| `_title` | Notification title |
| `_body` | Notification body |
| `_channel_id` | Android Notification Channel ID |
| Custom keys | Your custom data keys from the `data` payload |

---

## Recommended Flow

```
1. Initialize Firebase
2. Show in-app priming screen (why you want notifications)
3. On "Enable" → Firebase.messaging.request_permission()
4. On permission_result(true) → Firebase.messaging.get_token()
5. Store token server-side
```

## Platform Notes

- **Android 13+:** If the user denies permission twice, the system stops showing the dialog. You must then direct the user to app settings.
- **Background delivery:** If the message contains a `notification` block, the OS handles the UI. If it's a `data` message, the OS does not show a UI unless the app handles it (which this plugin does by emitting `notification_received`).
- **Token refresh:** Tokens can expire. Always listen for `token_received` to update your backend.
