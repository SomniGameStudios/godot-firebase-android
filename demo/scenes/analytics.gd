extends Control

@onready var output: RichTextLabel = %OutputPanel
var _collection_enabled := true

func _notification(what: int) -> void:
	if what == NOTIFICATION_WM_GO_BACK_REQUEST:
		get_tree().change_scene_to_packed(load("res://main.tscn"))

func _ready() -> void:
	Firebase.analytics.app_instance_id_result.connect(_on_instance_id)
	_log("platform", "Android")

# --- Events ---

func _on_log_event_pressed() -> void:
	_log("action", "Logging custom event 'test_event'...")
	Firebase.analytics.log_event("test_event", {
		"item_name": "sword_of_fire",
		"item_category": "weapons",
		"value": 100
	})
	_log("done", "Event logged (check Firebase Console)")

func _on_log_purchase_pressed() -> void:
	_log("action", "Logging purchase event...")
	Firebase.analytics.log_event("purchase", {
		"currency": "USD",
		"value": 9.99,
		"item_id": "gem_pack_100"
	})
	_log("done", "Purchase event logged")

# --- User ---

func _on_set_user_id_pressed() -> void:
	_log("action", "Setting user ID to 'test_user_123'...")
	Firebase.analytics.set_user_id("test_user_123")
	_log("done", "User ID set")

func _on_set_user_property_pressed() -> void:
	_log("action", "Setting user property 'favorite_food' = 'pizza'...")
	Firebase.analytics.set_user_property("favorite_food", "pizza")
	_log("done", "User property set")

func _on_set_default_params_pressed() -> void:
	_log("action", "Setting default event parameters...")
	Firebase.analytics.set_default_event_parameters({
		"app_version": "1.0.0",
		"platform": "android"
	})
	_log("done", "Default parameters set")

# --- Diagnostics ---

func _on_get_instance_id_pressed() -> void:
	_log("action", "Requesting app instance ID...")
	Firebase.analytics.get_app_instance_id()

func _on_instance_id(id: String) -> void:
	_log("instance_id", id if not id.is_empty() else "(empty)")

func _on_reset_analytics_pressed() -> void:
	_log("action", "Resetting analytics data...")
	Firebase.analytics.reset_analytics_data()
	_log("done", "Analytics data reset")

func _on_toggle_collection_pressed() -> void:
	_collection_enabled = not _collection_enabled
	_log("action", "Setting analytics collection to %s..." % str(_collection_enabled))
	Firebase.analytics.set_analytics_collection_enabled(_collection_enabled)
	_log("done", "Collection %s" % ("enabled" if _collection_enabled else "disabled"))

# --- Consent & Session (Android-only) ---

func _on_set_consent_granted_pressed() -> void:
	_log("action", "Granting all consent...")
	Firebase.analytics.set_consent(true, true, true, true)
	_log("done", "All consent granted")

func _on_set_consent_denied_pressed() -> void:
	_log("action", "Denying all consent...")
	Firebase.analytics.set_consent(false, false, false, false)
	_log("done", "All consent denied")

func _on_set_session_timeout_pressed() -> void:
	_log("action", "Setting session timeout to 300s...")
	Firebase.analytics.set_session_timeout(300)
	_log("done", "Session timeout set to 300s")

# --- Logging ---

func _log(context: String, message: String) -> void:
	var t = Time.get_time_string_from_system()
	output.text += "[%s] %s: %s\n" % [t, context, message]
	if not is_inside_tree():
		return
	await get_tree().process_frame
	if not is_inside_tree():
		return
	output.scroll_to_line(output.get_line_count())

func _on_clear_output_pressed() -> void:
	output.text = ""
