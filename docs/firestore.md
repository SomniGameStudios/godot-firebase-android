---
layout: default
title: Cloud Firestore
nav_order: 3
---

# Cloud Firestore

The Cloud Firestore module in **GodotFirebaseAndroid** supports adding, retrieving, updating, and deleting documents, as well as listening for document changes.

## Signals

- `write_task_completed(result: Dictionary)`
  Emitted after adding or setting a document.

- `get_task_completed(result: Dictionary)`
  Emitted after retrieving a document or a list of documents.

- `update_task_completed(result: Dictionary)`
  Emitted after updating a document.

- `delete_task_completed(result: Dictionary)`
  Emitted after deleting a document.

- `document_changed(document_path: String, data: Dictionary)`
  Emitted when a listened document is changed.

- `query_task_completed(result: Dictionary)`
  Emitted after a query operation completes. Contains a `documents` array of results.

- `collection_changed(collection_path: String, documents: Array)`
  Emitted when any document in a listened collection changes. Each element in the array is a dictionary with `docID` and `data` keys.

- `batch_task_completed(result: Dictionary)`
  Emitted after a WriteBatch commit completes.

- `transaction_task_completed(result: Dictionary)`
  Emitted after a transaction completes.

**Note**: All signal result dictionaries contain the following keys:

- status (bool): true if the operation succeeded, false otherwise.

- docID (String): The Firestore document ID related to the operation.

- data (optional Dictionary): The data returned in the operation (if applicable).

- error (optional String): The error message if the operation failed.

## Methods

{: .text-green-100 }
### add_document(collection: String, data: Dictionary)

Adds a new document to the specified collection with an auto-generated ID.

**Emits:** `write_task_completed`

```gdscript
Firebase.firestore.add_document("players", {"name": "Alice", "score": 100})
```

---

{: .text-green-100 }
### set_document(collection: String, documentId: String, data: Dictionary, merge: bool = false)

Sets data for a document. If the document exists, it will be overwritten unless `merge` is `true`.

**Emits:** `write_task_completed`

```gdscript
Firebase.firestore.set_document("players", "user_123", {"score": 150}, true)
```

---

{: .text-green-100 }
### get_document(collection: String, documentId: String)

Retrieves a single document from the specified collection.

**Emits:** `get_task_completed`

```gdscript
Firebase.firestore.get_document("players", "user_123")
```

---

{: .text-green-100 }
### get_documents_in_collection(collection: String)

Retrieves all documents in a given collection.

**Emits:** `get_task_completed`

```gdscript
Firebase.firestore.get_documents_in_collection("players")
```

---

{: .text-green-100 }
### update_document(collection: String, documentId: String, data: Dictionary)

Updates fields in a document without overwriting the entire document.

**Emits:** `update_task_completed`

```gdscript
Firebase.firestore.update_document("players", "user_123", {"score": 200})
```

---

{: .text-green-100 }
### `delete_document(collection: String, documentId: String)

Deletes a document from the specified collection.

**Emits:** `delete_task_completed`

```gdscript
Firebase.firestore.delete_document("players", "user_123")
```

---

{: .text-green-100 }
### listen_to_document(documentPath: String)

Starts listening to changes on a specific document path.

**Emits:** `document_changed`

```gdscript
Firebase.firestore.listen_to_document("players/user_123")
```

---

{: .text-green-100 }
### stop_listening_to_document(documentPath: String)

Stops listening to changes for a document path.

```gdscript
Firebase.firestore.stop_listening_to_document("players/user_123")
```

---

{: .text-green-100 }
### query_documents(collection: String, filters: Array = [], order_by: String = "", order_descending: bool = false, limit_count: int = 0)

Queries documents in a collection with optional filters, ordering, and limits. Each filter is a dictionary with `field`, `op`, and `value` keys.

Supported operators: `==`, `!=`, `<`, `<=`, `>`, `>=`, `array_contains`, `in`, `not_in`, `array_contains_any`.

**Emits:** `query_task_completed`

```gdscript
var filters = [
    {"field": "score", "op": ">=", "value": 100},
    {"field": "active", "op": "==", "value": true}
]
Firebase.firestore.query_documents("players", filters, "score", true, 10)
```

---

{: .text-green-100 }
### use_emulator(host: String, port: int)

Connects the Firestore module to a local Firebase Firestore emulator. Must be called before any other Firestore operations.

{: .warning }
Only use this during development. Do not ship with emulator enabled.

```gdscript
Firebase.firestore.use_emulator("10.0.2.2", 8080)
```

---

{: .text-green-100 }
### listen_to_collection(collection: String)

Starts listening to all changes in a collection. When any document is added, modified, or removed, the signal fires with the full list of documents.

**Emits:** `collection_changed`

```gdscript
Firebase.firestore.listen_to_collection("players")
```

---

{: .text-green-100 }
### stop_listening_to_collection(collection: String)

Stops listening to changes in a collection.

```gdscript
Firebase.firestore.stop_listening_to_collection("players")
```

---

## WriteBatch Operations

WriteBatch allows you to group multiple write operations into a single atomic commit. Either all operations succeed or none are applied.

{: .text-green-100 }
### create_batch() -> int

Creates a new WriteBatch and returns a batch ID used for subsequent batch operations.

```gdscript
var batch_id = Firebase.firestore.create_batch()
```

---

{: .text-green-100 }
### batch_set(batch_id: int, collection: String, document_id: String, data: Dictionary, merge: bool = false)

Adds a set operation to the batch.

```gdscript
Firebase.firestore.batch_set(batch_id, "players", "user_1", {"name": "Alice", "score": 100})
```

---

{: .text-green-100 }
### batch_update(batch_id: int, collection: String, document_id: String, data: Dictionary)

Adds an update operation to the batch. The document must already exist.

```gdscript
Firebase.firestore.batch_update(batch_id, "players", "user_1", {"score": 200})
```

---

{: .text-green-100 }
### batch_delete(batch_id: int, collection: String, document_id: String)

Adds a delete operation to the batch.

```gdscript
Firebase.firestore.batch_delete(batch_id, "players", "user_old")
```

---

{: .text-green-100 }
### commit_batch(batch_id: int)

Commits all operations in the batch atomically.

**Emits:** `batch_task_completed`

```gdscript
var batch_id = Firebase.firestore.create_batch()
Firebase.firestore.batch_set(batch_id, "players", "user_1", {"name": "Alice"})
Firebase.firestore.batch_update(batch_id, "players", "user_2", {"score": 300})
Firebase.firestore.batch_delete(batch_id, "players", "user_old")
Firebase.firestore.commit_batch(batch_id)
```

---

## Transactions

{: .text-green-100 }
### run_transaction(collection: String, document_id: String, update_data: Dictionary)

Runs a Firestore transaction that reads the document and then applies the update atomically. If another client modifies the document during the transaction, Firestore automatically retries.

**Emits:** `transaction_task_completed`

```gdscript
Firebase.firestore.run_transaction("players", "user_123", {"score": 500})
```

---

## FieldValue Helpers

Special values that can be used inside document data for atomic server-side operations. Use these as values in dictionaries passed to `add_document`, `set_document`, `update_document`, or batch operations.

{: .text-green-100 }
### server_timestamp() -> Dictionary

Returns a sentinel value that tells Firestore to use the server timestamp.

```gdscript
Firebase.firestore.update_document("players", "user_123", {
    "last_login": Firebase.firestore.server_timestamp()
})
```

---

{: .text-green-100 }
### array_union(elements: Array) -> Dictionary

Returns a sentinel value that adds elements to an array field without duplicates.

```gdscript
Firebase.firestore.update_document("players", "user_123", {
    "badges": Firebase.firestore.array_union(["gold", "silver"])
})
```

---

{: .text-green-100 }
### array_remove(elements: Array) -> Dictionary

Returns a sentinel value that removes elements from an array field.

```gdscript
Firebase.firestore.update_document("players", "user_123", {
    "badges": Firebase.firestore.array_remove(["bronze"])
})
```

---

{: .text-green-100 }
### increment_by(value: float) -> Dictionary

Returns a sentinel value that atomically increments a numeric field.

```gdscript
Firebase.firestore.update_document("players", "user_123", {
    "score": Firebase.firestore.increment_by(10)
})
```

---

{: .text-green-100 }
### delete_field() -> Dictionary

Returns a sentinel value that removes a field from a document.

```gdscript
Firebase.firestore.update_document("players", "user_123", {
    "deprecated_field": Firebase.firestore.delete_field()
})
```
