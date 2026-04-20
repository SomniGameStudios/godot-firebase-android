extends Node

var auth: Node
var firestore: Node
var realtimeDB: Node
var storage: Node
var analytics: Node
var remote_config: Node
var messaging: Node

func _enter_tree() -> void:
	auth = preload("res://addons/GodotFirebaseAndroid/modules/Auth.gd").new()
	firestore = preload("res://addons/GodotFirebaseAndroid/modules/Firestore.gd").new()
	realtimeDB = preload("res://addons/GodotFirebaseAndroid/modules/RealtimeDB.gd").new()
	storage = preload("res://addons/GodotFirebaseAndroid/modules/Storage.gd").new()
	analytics = preload("res://addons/GodotFirebaseAndroid/modules/Analytics.gd").new()
	remote_config = preload("res://addons/GodotFirebaseAndroid/modules/RemoteConfig.gd").new()
	messaging = preload("res://addons/GodotFirebaseAndroid/modules/Messaging.gd").new()
	
	add_child(auth)
	add_child(firestore)
	add_child(realtimeDB)
	add_child(storage)
	add_child(analytics)
	add_child(remote_config)
	add_child(messaging)

func _ready() -> void:
	if Engine.has_singleton("GodotFirebaseAndroid"):
		var _plugin_singleton := Engine.get_singleton("GodotFirebaseAndroid")
	
		auth._plugin_singleton = _plugin_singleton
		auth._connect_signals()
		
		firestore._plugin_singleton = _plugin_singleton
		firestore._connect_signals()
		
		realtimeDB._plugin_singleton = _plugin_singleton
		realtimeDB._connect_signals()
		
		storage._plugin_singleton = _plugin_singleton
		storage._connect_signals()

		analytics._plugin_singleton = _plugin_singleton
		analytics._connect_signals()

		remote_config._plugin_singleton = _plugin_singleton
		remote_config._connect_signals()

		messaging._plugin_singleton = _plugin_singleton
		messaging._connect_signals()
	else:
		if not OS.has_feature("editor"):
			printerr("GodotFirebaseAndroid singleton not found!")
		return
