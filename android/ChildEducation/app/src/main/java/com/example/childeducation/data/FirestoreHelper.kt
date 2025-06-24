package com.example.childeducation.data

import com.example.childeducation.viewmodel.HistoryEntry
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.ktx.firestore
import com.google.firebase.ktx.Firebase
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow
import kotlinx.coroutines.tasks.await

object FirestoreHelper {

    private val db = Firebase.firestore
    private val userId: String? get() = FirebaseAuth.getInstance().currentUser?.uid

    suspend fun saveLanguage(language: String) {
        userId?.let {
            db.collection("users").document(it)
                .set(mapOf("language" to language))
                .await()
        }
    }

    suspend fun getLanguage(): String? {
        return userId?.let {
            val snapshot = db.collection("users").document(it).get().await()
            snapshot.getString("language")
        }
    }

    suspend fun saveHistoryEntry(entry: String) {
        userId?.let {
            db.collection("users").document(it)
                .collection("history")
                .add(
                    mapOf(
                        "entry" to entry,
                        "timestamp" to System.currentTimeMillis()
                    )
                )
                .await()
        }
    }

    fun getHistoryFlow() = callbackFlow<List<HistoryEntry>> {
        val uid = userId
        if (uid == null) {
            trySend(emptyList())
            close()
            return@callbackFlow
        }

        val listener: ListenerRegistration = db.collection("users").document(uid)
            .collection("history")
            .orderBy("timestamp", com.google.firebase.firestore.Query.Direction.DESCENDING)
            .addSnapshotListener { snapshot, _ ->
                val history = snapshot?.documents?.mapNotNull {
                    val entry = it.getString("entry")
                    val timestamp = it.getLong("timestamp")
                    if (entry != null && timestamp != null) {
                        HistoryEntry(entry, timestamp)
                    } else null
                } ?: emptyList()
                trySend(history)
            }

        awaitClose { listener.remove() }
    }

    suspend fun clearAllHistory() {
        val uid = userId
        if (uid != null) {
            val collection = db.collection("users").document(uid).collection("history").get().await()
            for (doc in collection.documents) {
                doc.reference.delete().await()
            }
        }
    }
}
