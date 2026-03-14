extends Control

var auth = load("res://scenes/authentication.tscn")
var firestore = load("res://scenes/firestore.tscn")
var realtimeDB = load("res://scenes/realtime_db.tscn")
var storage = load("res://scenes/storage.tscn")
var remote_config = load("res://scenes/remote_config.tscn")
var analytics = load("res://scenes/analytics.tscn")

func _on_auth_pressed() -> void:
	get_tree().change_scene_to_packed(auth)


func _on_firestore_pressed() -> void:
	get_tree().change_scene_to_packed(firestore)


func _on_realtime_db_pressed() -> void:
	get_tree().change_scene_to_packed(realtimeDB)


func _on_storage_pressed() -> void:
	get_tree().change_scene_to_packed(storage)


func _on_remote_config_pressed() -> void:
	get_tree().change_scene_to_packed(remote_config)


func _on_analytics_pressed() -> void:
	get_tree().change_scene_to_packed(analytics)
