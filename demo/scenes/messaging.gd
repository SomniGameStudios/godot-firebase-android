extends Control

@onready var output: RichTextLabel = %OutputPanel

func _notification(what: int) -> void:
	if what == NOTIFICATION_WM_GO_BACK_REQUEST:
		get_tree().change_scene_to_packed(load("res://main.tscn"))

func _ready() -> void:
	Firebase.messaging.token_received.connect(_on_token_received)
	Firebase.messaging.token_error.connect(_on_token_error)
	Firebase.messaging.notification_received.connect(_on_notification_received)
	Firebase.messaging.notification_opened.connect(_on_notification_opened)
	Firebase.messaging.permission_result.connect(_on_permission_result)
	Firebase.messaging.topic_subscribe_success.connect(_on_topic_subscribe_success)
	Firebase.messaging.topic_subscribe_failure.connect(_on_topic_subscribe_failure)
	Firebase.messaging.topic_unsubscribe_success.connect(_on_topic_unsubscribe_success)
	Firebase.messaging.topic_unsubscribe_failure.connect(_on_topic_unsubscribe_failure)
	Firebase.messaging.token_delete_success.connect(_on_token_delete_success)
	Firebase.messaging.token_delete_failure.connect(_on_token_delete_failure)

	_log("ready", "Messaging module loaded. Requesting permissions...")
	Firebase.messaging.request_permission()

func _on_request_permission_pressed() -> void:
	_log("action", "Requesting permission...")
	Firebase.messaging.request_permission()

func _on_get_token_pressed() -> void:
	_log("action", "Requesting token...")
	Firebase.messaging.get_token()

func _on_delete_token_pressed() -> void:
	_log("action", "Deleting token...")
	Firebase.messaging.delete_token()

func _on_subscribe_topic_pressed() -> void:
	_log("action", "Subscribing to topic 'test_topic'...")
	Firebase.messaging.subscribe_to_topic("test_topic")

func _on_unsubscribe_topic_pressed() -> void:
	_log("action", "Unsubscribing from topic 'test_topic'...")
	Firebase.messaging.unsubscribe_from_topic("test_topic")

# --- Signals ---

func _on_permission_result(granted: bool) -> void:
	_log("permission", "Granted: %s" % granted)
	if granted:
		Firebase.messaging.get_token()

func _on_token_received(token: String) -> void:
	_log("token", token)

func _on_token_error(message: String) -> void:
	_log("token_error", message)

func _on_notification_received(data: Dictionary) -> void:
	_log("notification", str(data))

func _on_notification_opened(data: Dictionary) -> void:
	_log("notification_opened", str(data))

func _on_topic_subscribe_success(topic: String) -> void:
	_log("topic_sub", "Success: %s" % topic)

func _on_topic_subscribe_failure(message: String) -> void:
	_log("topic_sub_fail", message)

func _on_topic_unsubscribe_success(topic: String) -> void:
	_log("topic_unsub", "Success: %s" % topic)

func _on_topic_unsubscribe_failure(message: String) -> void:
	_log("topic_unsub_fail", message)

func _on_token_delete_success() -> void:
	_log("token_delete", "Success")

func _on_token_delete_failure(message: String) -> void:
	_log("token_delete_fail", message)

# --- Logging ---

func _log(context: String, message: String) -> void:
	var t = Time.get_time_string_from_system()
	var formatted = "[%s] %s: %s" % [t, context, message]
	print(formatted)
	output.text += formatted + "\n"
	output.call_deferred("scroll_to_line", output.get_line_count())

func _on_clear_output_pressed() -> void:
	output.text = ""
