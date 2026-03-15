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

# --- Signal handlers ---

func _on_fetch_completed(result: Dictionary) -> void:
	_log("fetch", "status=%s activated=%s error=%s" % [result.get("status"), result.get("activated", ""), result.get("error", "")])

func _on_activate_completed(result: Dictionary) -> void:
	_log("activate", "status=%s activated=%s error=%s" % [result.get("status"), result.get("activated", ""), result.get("error", "")])

func _on_config_updated(updated_keys) -> void:
	_log("update", "keys=%s" % str(updated_keys))

# --- Setup ---

func _on_initialize_pressed() -> void:
	_log("action", "Initializing Remote Config...")
	Firebase.remote_config.initialize()
	_log("done", "Remote Config initialized")

func _on_set_defaults_pressed() -> void:
	var defaults = {"welcome_message": "Hello!", "feature_enabled": false, "max_items": 10}
	_log("action", "Setting defaults: %s" % JSON.stringify(defaults))
	Firebase.remote_config.set_defaults(defaults)
	_log("done", "Defaults set")

func _on_set_dev_interval_pressed() -> void:
	_log("action", "Setting minimum fetch interval to 0 (dev mode)")
	Firebase.remote_config.set_minimum_fetch_interval(0)

func _on_set_fetch_timeout_pressed() -> void:
	_log("action", "Setting fetch timeout to 10s")
	Firebase.remote_config.set_fetch_timeout(10)

# --- Fetch operations ---

func _on_fetch_pressed() -> void:
	_log("action", "Fetching remote config...")
	Firebase.remote_config.fetch()

func _on_activate_pressed() -> void:
	_log("action", "Activating fetched config...")
	Firebase.remote_config.activate()

func _on_fetch_and_activate_pressed() -> void:
	_log("action", "Fetching and activating...")
	Firebase.remote_config.fetch_and_activate()

# --- Read values ---

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

func _on_get_all_pressed() -> void:
	var all_values = Firebase.remote_config.get_all()
	_log("all", JSON.stringify(all_values))

func _on_get_json_pressed() -> void:
	var key = key_input.text
	if key.is_empty():
		_log("error", "Enter a key name first")
		return
	var json_val = Firebase.remote_config.get_json(key)
	_log("json", "key='%s' json='%s'" % [key, json_val])

func _on_get_source_pressed() -> void:
	var key = key_input.text
	if key.is_empty():
		_log("error", "Enter a key name first")
		return
	var source = Firebase.remote_config.get_value_source(key)
	var source_names = {0: "static", 1: "default", 2: "remote"}
	_log("source", "key='%s' source=%s" % [key, source_names.get(source, "unknown")])

func _on_fetch_status_pressed() -> void:
	var status = Firebase.remote_config.get_last_fetch_status()
	var status_names = {-1: "success", 0: "no_fetch_yet", 1: "failure", 2: "throttled"}
	var fetch_time = Firebase.remote_config.get_last_fetch_time()
	_log("status", "last_fetch=%s time=%s" % [status_names.get(status, "unknown"), fetch_time])

# --- Real-time updates ---

func _on_listen_pressed() -> void:
	_log("action", "Listening for config updates...")
	Firebase.remote_config.listen_for_updates()

func _on_stop_listen_pressed() -> void:
	_log("action", "Stopped listening for config updates")
	Firebase.remote_config.stop_listening_for_updates()

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
