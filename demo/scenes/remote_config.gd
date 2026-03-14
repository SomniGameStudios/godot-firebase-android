extends Control

@onready var output: RichTextLabel = %OutputPanel
@onready var key_input: LineEdit = %KeyInput


func _notification(what: int) -> void:
	if what == NOTIFICATION_WM_GO_BACK_REQUEST:
		get_tree().change_scene_to_packed(load("res://main.tscn"))


func _ready() -> void:
	Firebase.remote_config.fetch_completed.connect(_on_fetch_completed)
	_log("platform", "Android")


func _on_fetch_completed(result: Dictionary) -> void:
	_log("fetch", "status=%s activated=%s error=%s" % [result.get("status"), result.get("activated", ""), result.get("error", "")])


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
