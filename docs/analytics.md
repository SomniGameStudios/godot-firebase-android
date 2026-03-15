---
layout: default
title: Analytics
nav_order: 6
---

# Firebase Analytics

The Firebase Analytics module in **GodotFirebaseAndroid** allows you to log events, set user properties, manage consent, and retrieve the app instance ID.

## Signals

- `app_instance_id_result(id: String)`
  Emitted after requesting the app instance ID. Returns an empty string on failure.

## Methods

{: .text-green-100 }
### log_event(name: String, parameters: Dictionary)

Logs a custom analytics event. Parameter values can be `String`, `int`, `float`, or `bool`.

```gdscript
Firebase.analytics.log_event("level_complete", {"level": 5, "score": 1200})
```

---

{: .text-green-100 }
### set_user_property(name: String, value: String)

Sets a user property for analytics segmentation. Up to 25 user properties can be set per project.

```gdscript
Firebase.analytics.set_user_property("favorite_food", "pizza")
```

---

{: .text-green-100 }
### set_user_id(id: String)

Sets the user ID for analytics. This is useful for linking analytics data to your own user system.

```gdscript
Firebase.analytics.set_user_id("user_123")
```

---

{: .text-green-100 }
### set_analytics_collection_enabled(enabled: bool)

Enables or disables analytics data collection. When disabled, no events are logged.

```gdscript
Firebase.analytics.set_analytics_collection_enabled(false)  # opt out
```

---

{: .text-green-100 }
### reset_analytics_data()

Clears all analytics data from the device and resets the app instance ID.

```gdscript
Firebase.analytics.reset_analytics_data()
```

---

{: .text-green-100 }
### set_default_event_parameters(parameters: Dictionary)

Sets default parameters that are sent with every subsequent event. Useful for values like app version or user segment that apply to all events.

```gdscript
Firebase.analytics.set_default_event_parameters({"app_version": "1.2.0", "platform": "android"})
```

---

{: .text-green-100 }
### get_app_instance_id()

Requests the unique app instance ID assigned by Firebase Analytics. The result is returned asynchronously via signal.

**Emits:** `app_instance_id_result`

```gdscript
Firebase.analytics.get_app_instance_id()
# Listen for the result:
# Firebase.analytics.app_instance_id_result.connect(_on_instance_id)
```

---

{: .text-green-100 }
### set_consent(ad_storage: bool, analytics_storage: bool, ad_user_data: bool, ad_personalization: bool)

Sets the consent status for various data collection purposes. Use this to comply with consent requirements (e.g., GDPR).

| Parameter | Purpose |
|---|---|
| `ad_storage` | Enables storage of advertising-related data |
| `analytics_storage` | Enables storage of analytics-related data |
| `ad_user_data` | Allows sending user data to Google for advertising |
| `ad_personalization` | Allows personalized advertising |

```gdscript
# Grant all consent
Firebase.analytics.set_consent(true, true, true, true)

# Deny advertising, allow analytics
Firebase.analytics.set_consent(false, true, false, false)
```

---

{: .text-green-100 }
### set_session_timeout(seconds: int)

Sets the duration of inactivity (in seconds) before a session expires. Default is 1800 seconds (30 minutes).

```gdscript
Firebase.analytics.set_session_timeout(600)  # 10 minutes
```
