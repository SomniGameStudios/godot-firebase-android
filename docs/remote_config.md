---
layout: default
title: Remote Config
nav_order: 7
---

# Remote Config

The Remote Config module in **GodotFirebaseAndroid** lets you fetch and activate configuration values from the Firebase console, enabling you to change your app's behavior and appearance without publishing an update.

## Signals

- `fetch_completed(result: Dictionary)`
  Emitted after a fetch or fetch-and-activate operation completes. The result dictionary contains:
  - `status` (bool): `true` if the fetch succeeded
  - `activated` (bool): `true` if new values were activated (only present for `fetch_and_activate` and `activate`)
  - `error` (String): Error message if the operation failed

- `activate_completed(result: Dictionary)`
  Emitted after an activate operation completes. Same keys as `fetch_completed`.

- `config_updated(updated_keys: Array)`
  Emitted when config values are updated in real-time. Contains an array of the keys that changed.

## Methods

{: .text-green-100 }
### initialize()

Initializes Remote Config with the current settings. Call this once at startup.

```gdscript
Firebase.remote_config.initialize()
```

---

{: .text-green-100 }
### set_defaults(defaults: Dictionary)

Sets default config values that are used before fetched values are available.

```gdscript
Firebase.remote_config.set_defaults({
    "welcome_message": "Hello!",
    "max_retries": 3,
    "feature_enabled": true
})
```

---

{: .text-green-100 }
### set_minimum_fetch_interval(seconds: int)

Sets the minimum interval between fetch requests. During development, set this to `0` to fetch on every request. Default is 3600 seconds (1 hour).

{: .note }
Firebase throttles fetch requests. In production, keep this at 3600 or higher to avoid throttling.

```gdscript
Firebase.remote_config.set_minimum_fetch_interval(0)    # development
Firebase.remote_config.set_minimum_fetch_interval(3600)  # production
```

---

{: .text-green-100 }
### set_fetch_timeout(seconds: int)

Sets the timeout for fetch requests. Default is 60 seconds.

```gdscript
Firebase.remote_config.set_fetch_timeout(30)
```

---

{: .text-green-100 }
### fetch()

Fetches config values from Firebase without activating them. Fetched values are not available until `activate()` is called.

**Emits:** `fetch_completed`

```gdscript
Firebase.remote_config.fetch()
```

---

{: .text-green-100 }
### activate()

Activates the most recently fetched config values, making them available to the getters.

**Emits:** `activate_completed`

```gdscript
Firebase.remote_config.activate()
```

---

{: .text-green-100 }
### fetch_and_activate()

Fetches and immediately activates config values in a single operation.

**Emits:** `fetch_completed`

```gdscript
Firebase.remote_config.fetch_and_activate()
```

---

## Typed Getters

These methods return config values after they have been fetched and activated. If a key has not been fetched, the default value (from `set_defaults` or the type's zero value) is returned.

{: .text-green-100 }
### get_string(key: String) -> String

Returns a config value as a string.

```gdscript
var message = Firebase.remote_config.get_string("welcome_message")
```

---

{: .text-green-100 }
### get_bool(key: String) -> bool

Returns a config value as a boolean.

```gdscript
var enabled = Firebase.remote_config.get_bool("feature_enabled")
```

---

{: .text-green-100 }
### get_int(key: String) -> int

Returns a config value as an integer.

```gdscript
var retries = Firebase.remote_config.get_int("max_retries")
```

---

{: .text-green-100 }
### get_float(key: String) -> float

Returns a config value as a float.

```gdscript
var multiplier = Firebase.remote_config.get_float("score_multiplier")
```

---

{: .text-green-100 }
### get_json(key: String) -> String

Returns a config value as a raw JSON string. Use `JSON.parse_string()` to parse it.

```gdscript
var json_str = Firebase.remote_config.get_json("level_config")
var data = JSON.parse_string(json_str)
```

---

{: .text-green-100 }
### get_all() -> Dictionary

Returns all config values as a dictionary of strings (keys to string values).

```gdscript
var all_config = Firebase.remote_config.get_all()
for key in all_config:
    print("%s = %s" % [key, all_config[key]])
```

---

## Status & Metadata

{: .text-green-100 }
### get_value_source(key: String) -> int

Returns the source of a config value. Useful for debugging whether a value comes from the server, defaults, or is static.

| Value | Constant | Meaning |
|---|---|---|
| `0` | Static | No value set anywhere |
| `1` | Default | Using the default value from `set_defaults` |
| `2` | Remote | Using a value fetched from Firebase |

```gdscript
var source = Firebase.remote_config.get_value_source("welcome_message")
```

---

{: .text-green-100 }
### get_last_fetch_status() -> int

Returns the status of the last fetch attempt.

| Value | Meaning |
|---|---|
| `-1` | Success |
| `0` | No fetch yet |
| `1` | Failure |
| `2` | Throttled |

```gdscript
var status = Firebase.remote_config.get_last_fetch_status()
```

---

{: .text-green-100 }
### get_last_fetch_time() -> String

Returns the time of the last successful fetch as an ISO 8601 string (e.g., `"2025-01-15T10:30:00Z"`). Returns an empty string if no fetch has occurred.

```gdscript
var time = Firebase.remote_config.get_last_fetch_time()
```

---

## Real-Time Updates

{: .text-green-100 }
### listen_for_updates()

Starts listening for real-time config updates from Firebase. When config values are changed in the Firebase console, the `config_updated` signal fires with the list of changed keys.

**Emits:** `config_updated` when remote values change.

{: .note }
After receiving a `config_updated` signal, you still need to call `activate()` to apply the new values.

```gdscript
Firebase.remote_config.listen_for_updates()

# In your signal handler:
func _on_config_updated(updated_keys: Array):
    print("Keys changed: ", updated_keys)
    Firebase.remote_config.activate()
```

---

{: .text-green-100 }
### stop_listening_for_updates()

Stops listening for real-time config updates.

```gdscript
Firebase.remote_config.stop_listening_for_updates()
```
