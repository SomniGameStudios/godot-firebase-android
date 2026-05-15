extends Control

@onready var log_label := $MarginContainer/VBoxContainer/RichTextLabel

func _ready() -> void:
	if not Engine.has_singleton("GodotFirebaseAndroid"):
		_log("ERROR: GodotFirebaseAndroid singleton not found!")
		return
	
	_log("Starting Quick RTDB Test...")
	
	# Connect signals
	Firebase.realtimeDB.write_task_completed.connect(func(res): _log("Write OK: " + str(res)))
	Firebase.realtimeDB.get_task_completed.connect(func(res): _log("Read OK: " + str(res)))
	Firebase.realtimeDB.update_task_completed.connect(func(res): _log("Update OK: " + str(res)))
	
	# Test sequence
	_test_sequence()

func _test_sequence() -> void:
	var test_path := "debug_test/data"
	
	# 1. Set Value
	_log("1. Testing set_value...")
	Firebase.realtimeDB.set_value(test_path, {"status": "online", "val": 10})
	
	await get_tree().create_timer(2.0).timeout
	
	# 2. Increment (New PR feature!)
	_log("2. Testing increment (+5)...")
	var inc_data = Firebase.realtimeDB.increment(5)
	Firebase.realtimeDB.update_value(test_path, {"val": inc_data})
	
	await get_tree().create_timer(2.0).timeout
	
	# 3. Get Value
	_log("3. Testing get_value...")
	Firebase.realtimeDB.get_value(test_path)
	
	await get_tree().create_timer(2.0).timeout
	
	# 4. Listen/Stop Listening Test
	_log("4. Testing Listen/Stop logic...")
	var listener_path := "debug_test/listener"
	var test_state := {"count": 0}
	
	var listener_callable = func(path, data):
		if path == listener_path:
			test_state["count"] += 1
			_log("   [Signal] Received update for %s (Total: %d)" % [path, test_state["count"]])
	
	Firebase.realtimeDB.db_value_changed.connect(listener_callable)
	
	_log("   Starting listen...")
	Firebase.realtimeDB.listen_to_path(listener_path)
	
	# Wait for initial signal (Firebase always sends current state on attach)
	await get_tree().create_timer(1.0).timeout
	
	_log("   Triggering change 1 (Expect signal)...")
	Firebase.realtimeDB.set_value(listener_path, {"test": 1})
	
	await get_tree().create_timer(2.0).timeout
	
	_log("   Stopping listen...")
	Firebase.realtimeDB.stop_listening(listener_path)
	
	await get_tree().create_timer(1.0).timeout
	_log("   Triggering change 2 (Expect NO signal)...")
	Firebase.realtimeDB.set_value(listener_path, {"test": 2})
	
	await get_tree().create_timer(2.0).timeout
	
	# Expected: 1 (initial) + 1 (change 1) = 2 signals
	if test_state["count"] == 2:
		_log("SUCCESS: Stop listening worked (Total signals: 2).")
	else:
		_log("FAILURE: Stop listening failed! Received %d signals (Expected 2)." % test_state["count"])
	
	Firebase.realtimeDB.db_value_changed.disconnect(listener_callable)

func _log(msg: String) -> void:
	print("[RTDB-TEST] ", msg)
	if log_label:
		log_label.text += "[%s] %s\n" % [Time.get_time_string_from_system(), msg]

func _on_back_pressed() -> void:
	get_tree().change_scene_to_file("res://main.tscn")
