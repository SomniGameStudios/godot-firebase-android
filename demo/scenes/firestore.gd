extends Control

@onready var output_panel = $MarginContainer/VBoxContainer/OutputPanel
@onready var collection = $MarginContainer/VBoxContainer/ScrollContainer/ButtonContainer/HBoxContainer/collection
@onready var docID = $MarginContainer/VBoxContainer/ScrollContainer/ButtonContainer/HBoxContainer/docID
@onready var pair_container = $ManageDataPanel/VBoxContainer/ScrollContainer/key_value_pair_container

func _notification(what: int) -> void:
	if what == NOTIFICATION_WM_GO_BACK_REQUEST:
		get_tree().change_scene_to_packed(load("res://main.tscn"))


func _ready() -> void:
	Firebase.firestore.write_task_completed.connect(print_output.bind("write_task_completed"))
	Firebase.firestore.get_task_completed.connect(print_output.bind("get_task_completed"))
	Firebase.firestore.update_task_completed.connect(print_output.bind("update_task_completed"))
	Firebase.firestore.delete_task_completed.connect(print_output.bind("delete_task_completed"))
	Firebase.firestore.document_changed.connect(print_listner_output.bind("document_changed"))
	Firebase.firestore.query_task_completed.connect(print_output.bind("query_task_completed"))
	Firebase.firestore.collection_changed.connect(print_listner_output.bind("collection_changed"))
	Firebase.firestore.batch_task_completed.connect(print_output.bind("batch_task_completed"))
	Firebase.firestore.transaction_task_completed.connect(print_output.bind("transaction_task_completed"))


func _log(message: String) -> void:
	var time = Time.get_time_string_from_system()
	output_panel.text += "[%s] %s\n" % [time, message]


func get_dictionary_from_inputs() -> Dictionary:
	var data_dict := Dictionary()
	for pair in pair_container.get_children():
		if pair.name == "sample_pair":
			continue
		var key = pair.get_child(0).text
		var value = pair.get_child(1).text
		if pair.get_child(2).button_pressed:
			value = int(value)
		data_dict[key] = value
	return data_dict


func print_output(arg, context: String):
	_log(context + ": " + str(arg))


func print_listner_output(arg, arg2, context: String):
	_log(context + ": " + str(arg) + " -|- " + str(arg2))


func _on_clear_output_pressed() -> void:
	output_panel.text = ""


func _on_add_document_pressed() -> void:
	Firebase.firestore.add_document(collection.text, get_dictionary_from_inputs())


func _on_set_document_pressed() -> void:
	Firebase.firestore.set_document(collection.text, docID.text, get_dictionary_from_inputs(), $MarginContainer/VBoxContainer/ScrollContainer/ButtonContainer/HBoxContainer2/merge.button_pressed)


func _on_get_document_pressed() -> void:
	Firebase.firestore.get_document(collection.text, docID.text)


func _on_get_documents_in_collection_pressed() -> void:
	Firebase.firestore.get_documents_in_collection(collection.text)


func _on_update_document_pressed() -> void:
	Firebase.firestore.update_document(collection.text, docID.text, get_dictionary_from_inputs())


func _on_delete_document_pressed() -> void:
	Firebase.firestore.delete_document(collection.text, docID.text)


func _on_listen_to_document_pressed() -> void:
	Firebase.firestore.listen_to_document(collection.text + "/" + docID.text)


func _on_stop_listening_to_document_pressed() -> void:
	Firebase.firestore.stop_listening_to_document(collection.text + "/" + docID.text)


func _on_query_documents_pressed() -> void:
	var filters = [{"field": "score", "op": ">=", "value": 100}]
	_log("query: collection=%s filters=%s" % [collection.text, str(filters)])
	Firebase.firestore.query_documents(collection.text, filters, "", false, 10)


func _on_listen_to_collection_pressed() -> void:
	_log("Listening to collection: %s" % collection.text)
	Firebase.firestore.listen_to_collection(collection.text)


func _on_stop_listening_collection_pressed() -> void:
	_log("Stopped listening to collection: %s" % collection.text)
	Firebase.firestore.stop_listening_to_collection(collection.text)


func _on_run_batch_demo_pressed() -> void:
	var batch_id = Firebase.firestore.create_batch()
	_log("Created batch %s" % str(batch_id))
	Firebase.firestore.batch_set(batch_id, collection.text, "batch_doc_1", {"name": "Batch Set", "value": 1}, false)
	Firebase.firestore.batch_update(batch_id, collection.text, docID.text, {"batch_updated": true})
	Firebase.firestore.commit_batch(batch_id)
	_log("Commit requested for batch %s" % str(batch_id))


func _on_run_transaction_pressed() -> void:
	_log("Running transaction on %s/%s" % [collection.text, docID.text])
	Firebase.firestore.run_transaction(collection.text, docID.text, get_dictionary_from_inputs())


func _on_update_timestamp_pressed() -> void:
	_log("Updating %s/%s with server timestamp" % [collection.text, docID.text])
	Firebase.firestore.update_document(collection.text, docID.text, {"updated_at": Firebase.firestore.server_timestamp()})


func _on_increment_field_pressed() -> void:
	_log("Incrementing 'count' by 1 on %s/%s" % [collection.text, docID.text])
	Firebase.firestore.update_document(collection.text, docID.text, {"count": Firebase.firestore.increment_by(1)})


func _on_use_emulator_pressed() -> void:
	Firebase.firestore.use_emulator("10.0.2.2", 8080)
	_log("Firestore emulator set to 10.0.2.2:8080")


func _on_manage_data_pressed() -> void:
	$ManageDataPanel.show()


func _on_add_pair_pressed() -> void:
	var new_pair = $ManageDataPanel/VBoxContainer/ScrollContainer/key_value_pair_container/sample_pair.duplicate()
	new_pair.show()
	pair_container.add_child(new_pair)


func _on_close_pressed() -> void:
	$ManageDataPanel.hide()
