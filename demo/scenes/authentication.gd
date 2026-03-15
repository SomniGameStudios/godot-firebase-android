extends Control

@onready var output_panel = $MarginContainer/VBoxContainer/OutputPanel

@onready var email = $MarginContainer/VBoxContainer/ScrollContainer/ButtonContainer/LineEdit
@onready var password = $MarginContainer/VBoxContainer/ScrollContainer/ButtonContainer/LineEdit2
@onready var display_name_input = $MarginContainer/VBoxContainer/ScrollContainer/ButtonContainer/display_name_input

func _notification(what: int) -> void:
	if what == NOTIFICATION_WM_GO_BACK_REQUEST:
		get_tree().change_scene_to_packed(load("res://main.tscn"))


func _ready() -> void:
	Firebase.auth.auth_success.connect(print_output.bind("auth_success"))
	Firebase.auth.auth_failure.connect(print_output.bind("auth_failure"))
	Firebase.auth.link_with_google_success.connect(print_output.bind("link_with_google_success"))
	Firebase.auth.link_with_google_failure.connect(print_output.bind("link_with_google_failure"))
	Firebase.auth.sign_out_success.connect(print_output.bind("sign_out_success"))
	Firebase.auth.email_verification_sent.connect(print_output.bind("email_verification_sent"))
	Firebase.auth.password_reset_sent.connect(print_output.bind("password_reset_sent"))
	Firebase.auth.user_deleted.connect(print_output.bind("user_deleted"))
	Firebase.auth.auth_state_changed.connect(_on_auth_state_changed)
	Firebase.auth.id_token_result.connect(print_output.bind("id_token_result"))
	Firebase.auth.id_token_error.connect(print_output.bind("id_token_error"))
	Firebase.auth.profile_updated.connect(print_output.bind("profile_updated"))
	Firebase.auth.profile_update_failure.connect(print_output.bind("profile_update_failure"))


func _log(message: String) -> void:
	var time = Time.get_time_string_from_system()
	output_panel.text += "[%s] %s\n" % [time, message]


func print_output(arg, context: String):
	_log(context + ": " + str(arg))


func _on_clear_output_pressed() -> void:
	output_panel.text = ""


func _on_anonymous_sign_in_pressed() -> void:
	Firebase.auth.sign_in_anonymously()


func _on_email_sign_up_pressed() -> void:
	Firebase.auth.create_user_with_email_password(email.text, password.text)


func _on_email_sign_in_pressed() -> void:
	Firebase.auth.sign_in_with_email_password(email.text, password.text)


func _on_google_sign_in_pressed() -> void:
	Firebase.auth.sign_in_with_google()


func _on_link_anonymous_with_google_pressed() -> void:
	Firebase.auth.link_anonymous_with_google()


func _on_get_user_data_pressed() -> void:
	print_output(Firebase.auth.get_current_user_data(), "Current User Data")


func _on_is_signed_in_pressed() -> void:
	print_output(Firebase.auth.is_signed_in(), "Is SignedIn")


func _on_sign_out_pressed() -> void:
	Firebase.auth.sign_out()


func _on_delete_user_pressed() -> void:
	Firebase.auth.delete_current_user()


func _on_email_verification_pressed() -> void:
	Firebase.auth.send_email_verification()


func _on_password_reset_pressed() -> void:
	Firebase.auth.send_password_reset_email(email.text)


func _on_auth_state_changed(signed_in: bool, current_user_data) -> void:
	_log("auth_state_changed: signed_in=" + str(signed_in) + " data=" + str(current_user_data))


func _on_reauthenticate_pressed() -> void:
	Firebase.auth.reauthenticate_with_email(email.text, password.text)


func _on_add_auth_listener_pressed() -> void:
	Firebase.auth.add_auth_state_listener()
	_log("Auth state listener added")


func _on_remove_auth_listener_pressed() -> void:
	Firebase.auth.remove_auth_state_listener()
	_log("Auth state listener removed")


func _on_get_id_token_pressed() -> void:
	Firebase.auth.get_id_token(false)


func _on_update_profile_pressed() -> void:
	Firebase.auth.update_profile(display_name_input.text)


func _on_update_password_pressed() -> void:
	Firebase.auth.update_password(password.text)


func _on_reload_user_pressed() -> void:
	Firebase.auth.reload_user()


func _on_unlink_google_pressed() -> void:
	Firebase.auth.unlink_provider("google.com")


func _on_use_emulator_pressed() -> void:
	Firebase.auth.use_emulator("10.0.2.2", 9099)
	_log("Auth emulator set to 10.0.2.2:9099")
