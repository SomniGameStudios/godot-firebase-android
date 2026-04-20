extends Node

signal write_task_completed(result: Dictionary)
signal get_task_completed(result: Dictionary)
signal update_task_completed(result: Dictionary)
signal delete_task_completed(result: Dictionary)
signal value_changed(path: String, value: Variant)
signal query_task_completed(result: Dictionary)

var _plugin_singleton: Object

func _connect_signals():
	if not _plugin_singleton:
		return
	_plugin_singleton.connect("realtime_db_write_task_completed", write_task_completed.emit)
	_plugin_singleton.connect("realtime_db_get_task_completed", get_task_completed.emit)
	_plugin_singleton.connect("realtime_db_update_task_completed", update_task_completed.emit)
	_plugin_singleton.connect("realtime_db_delete_task_completed", delete_task_completed.emit)
	_plugin_singleton.connect("realtime_db_value_changed", value_changed.emit)
	_plugin_singleton.connect("realtime_db_query_task_completed", query_task_completed.emit)

func set_value(path: String, value: Variant) -> void:
	if _plugin_singleton:
		_plugin_singleton.realtimeDbSetValue(path, value)

func get_value(path: String) -> void:
	if _plugin_singleton:
		_plugin_singleton.realtimeDbGetValue(path)

func update_children(path: String, values: Dictionary) -> void:
	if _plugin_singleton:
		_plugin_singleton.realtimeDbUpdateChildren(path, values)

func delete_value(path: String) -> void:
	if _plugin_singleton:
		_plugin_singleton.realtimeDbDeleteValue(path)

func push_value(path: String, value: Variant) -> void:
	if _plugin_singleton:
		_plugin_singleton.realtimeDbPushValue(path, value)

func listen_to_value(path: String) -> void:
	if _plugin_singleton:
		_plugin_singleton.realtimeDbListenToValue(path)

func stop_listening_to_value(path: String) -> void:
	if _plugin_singleton:
		_plugin_singleton.realtimeDbStopListeningToValue(path)

func use_emulator(host: String, port: int) -> void:
	if _plugin_singleton:
		_plugin_singleton.realtimeDbUseEmulator(host, port)

func query_values(path: String, order_by: String = "", order_type: String = "key", start_at: Variant = null, end_at: Variant = null, equal_to: Variant = null, limit_to_first: int = 0, limit_to_last: int = 0) -> void:
	if _plugin_singleton:
		_plugin_singleton.realtimeDbQueryValues(path, order_by, order_type, start_at, end_at, equal_to, limit_to_first, limit_to_last)

func server_timestamp() -> Dictionary:
	if _plugin_singleton:
		return _plugin_singleton.realtimeDbServerTimestamp()
	return {}

func increment_by(value: float) -> Dictionary:
	if _plugin_singleton:
		return _plugin_singleton.realtimeDbIncrementBy(value)
	return {}
