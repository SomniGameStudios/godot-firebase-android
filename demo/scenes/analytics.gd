extends Control

@onready var output: RichTextLabel = %OutputPanel
var _collection_enabled := true


func _notification(what: int) -> void:
	if what == NOTIFICATION_WM_GO_BACK_REQUEST:
		get_tree().change_scene_to_packed(load("res://main.tscn"))


func _ready() -> void:
	_log("platform", "Android")


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


func _on_set_user_id_pressed() -> void:
	_log("action", "Setting user ID to 'test_user_123'...")
	Firebase.analytics.set_user_id("test_user_123")
	_log("done", "User ID set")


func _on_set_user_property_pressed() -> void:
	_log("action", "Setting user property 'favorite_food' = 'pizza'...")
	Firebase.analytics.set_user_property("favorite_food", "pizza")
	_log("done", "User property set")


func _on_reset_analytics_pressed() -> void:
	_log("action", "Resetting analytics data...")
	Firebase.analytics.reset_analytics_data()
	_log("done", "Analytics data reset")


func _on_toggle_collection_pressed() -> void:
	_collection_enabled = not _collection_enabled
	_log("action", "Setting analytics collection to %s..." % str(_collection_enabled))
	Firebase.analytics.set_analytics_collection_enabled(_collection_enabled)
	_log("done", "Collection %s" % ("enabled" if _collection_enabled else "disabled"))


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
