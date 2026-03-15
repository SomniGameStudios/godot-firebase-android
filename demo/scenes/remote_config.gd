extends Control

@onready var output: RichTextLabel = %OutputPanel
@onready var key_input: LineEdit = %KeyInput


func _notification(what: int) -> void:
	if what == NOTIFICATION_WM_GO_BACK_REQUEST:
		get_tree().change_scene_to_packed(load("res://main.tscn"))


func _ready() -> void:
	Firebase.remote_config.fetch_completed.connect(_on_fetch_completed)
	Firebase.remote_config.activate_completed.connect(_on_activate_completed)
	Firebase.remote_config.config_updated.connect(_on_config_updated)
	_log("platform", "Android")


func _on_fetch_completed(result: Dictionary) -> void:
	_log("fetch", "status=%s activated=%s error=%s" % [result.get("status"), result.get("activated", ""), result.get("error", "")])


func _on_activate_completed(result: Dictionary) -> void:
	_log("activate", "status=%s activated=%s error=%s" % [result.get("status"), result.get("activated", ""), result.get("error", "")])


func _on_config_updated(updated_keys) -> void:
	_log("config_updated", "keys=%s" % str(updated_keys))


func _on_initialize_pressed() -> void:
	_log("action", "Initializing Remote Config...")
	Firebase.remote_config.initialize()
	_log("done", "Remote Config initialized")


func _on_set_defaults_pressed() -> void:
	var defaults = {"welcome_message": "Hello!", "feature_enabled": false, "max_items": 10}
	_log("action", "Setting defaults: %s" % JSON.stringify(defaults))
	Firebase.remote_config.set_defaults(defaults)
	_log("done", "Defaults set")


func _on_fetch_and_activate_pressed() -> void:
	_log("action", "Fetching and activating...")
	Firebase.remote_config.fetch_and_activate()


func _on_get_value_pressed() -> void:
	var key = key_input.text
	if key.is_empty():
		_log("error", "Enter a key name first")
		return
	var str_val = Firebase.remote_config.get_string(key)
	var bool_val = Firebase.remote_config.get_bool(key)
	var int_val = Firebase.remote_config.get_int(key)
	var float_val = Firebase.remote_config.get_float(key)
	_log("value", "key='%s' string='%s' bool=%s int=%s float=%s" % [key, str_val, bool_val, int_val, float_val])


func _on_fetch_pressed() -> void:
	_log("action", "Fetching...")
	Firebase.remote_config.fetch()


func _on_activate_pressed() -> void:
	_log("action", "Activating...")
	Firebase.remote_config.activate()


func _on_get_all_pressed() -> void:
	var all_values = Firebase.remote_config.get_all()
	_log("get_all", str(all_values))


func _on_get_json_pressed() -> void:
	var key = key_input.text
	if key.is_empty():
		_log("error", "Enter a key name first")
		return
	var json_val = Firebase.remote_config.get_json(key)
	_log("get_json", "key='%s' json='%s'" % [key, json_val])


func _on_get_value_source_pressed() -> void:
	var key = key_input.text
	if key.is_empty():
		_log("error", "Enter a key name first")
		return
	var source = Firebase.remote_config.get_value_source(key)
	var source_name = ["Static", "Default", "Remote"][source] if source >= 0 and source <= 2 else "Unknown"
	_log("value_source", "key='%s' source=%s (%s)" % [key, source, source_name])


func _on_get_last_fetch_status_pressed() -> void:
	var status = Firebase.remote_config.get_last_fetch_status()
	var status_names = {-1: "Success", 0: "No fetch yet", 1: "Failure", 2: "Throttled"}
	_log("fetch_status", "%s (%s)" % [status, status_names.get(status, "Unknown")])


func _on_get_last_fetch_time_pressed() -> void:
	var time_str = Firebase.remote_config.get_last_fetch_time()
	_log("fetch_time", time_str if not time_str.is_empty() else "(no fetch yet)")


func _on_set_fetch_interval_pressed() -> void:
	Firebase.remote_config.set_minimum_fetch_interval(0)
	_log("done", "Fetch interval set to 0s (development mode)")


func _on_set_fetch_timeout_pressed() -> void:
	Firebase.remote_config.set_fetch_timeout(10)
	_log("done", "Fetch timeout set to 10s")


func _on_listen_for_updates_pressed() -> void:
	Firebase.remote_config.listen_for_updates()
	_log("done", "Listening for real-time config updates")


func _on_stop_listening_pressed() -> void:
	Firebase.remote_config.stop_listening_for_updates()
	_log("done", "Stopped listening for config updates")


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
