signal upload_completed(result: Dictionary)
signal download_task_completed(result: Dictionary)
signal delete_task_completed(result: Dictionary)
signal list_task_completed(result: Dictionary)

var _plugin_singleton: Object

func _connect_signals():
	if not _plugin_singleton:
		return
	_plugin_singleton.connect("storage_upload_task_completed", upload_completed.emit)
	_plugin_singleton.connect("storage_download_task_completed", download_task_completed.emit)
	_plugin_singleton.connect("storage_delete_task_completed", delete_task_completed.emit)
	_plugin_singleton.connect("storage_list_task_completed", list_task_completed.emit)

func upload_file(local_path: String, storage_path: String) -> void:
	if _plugin_singleton:
		_plugin_singleton.storageUploadFile(local_path, storage_path)

func download_file(storage_path: String, local_path: String) -> void:
	if _plugin_singleton:
		_plugin_singleton.storageDownloadFile(storage_path, local_path)

func delete_file(storage_path: String) -> void:
	if _plugin_singleton:
		_plugin_singleton.storageDeleteFile(storage_path)

func list_files(storage_path: String) -> void:
	if _plugin_singleton:
		_plugin_singleton.storageListFiles(storage_path)

func get_download_url(storage_path: String) -> void:
	if _plugin_singleton:
		_plugin_singleton.storageGetDownloadUrl(storage_path)

func use_emulator(host: String, port: int) -> void:
	if _plugin_singleton:
		_plugin_singleton.storageUseEmulator(host, port)
