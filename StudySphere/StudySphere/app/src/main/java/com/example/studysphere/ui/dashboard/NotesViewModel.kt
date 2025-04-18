// NotesViewModel.kt
package com.example.studysphere.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.studysphere.data.model.File
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.UUID

class NotesViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _notes = MutableLiveData<List<File>>()
    val notes: LiveData<List<File>> = _notes

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    fun loadNotes(courseId: String) {
        db.collection("files")
            .whereEqualTo("courseId", courseId)
            .addSnapshotListener { snapshot, error ->
                if (error != null) {
                    _errorMessage.value = "Error loading notes: ${error.message}"
                    return@addSnapshotListener
                }

                val notesList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(File::class.java)
                } ?: emptyList()

                _notes.value = notesList.sortedByDescending { it.createdAt }
            }
    }

    fun createNote(title: String, content: String, courseId: String) {
        val currentUser = auth.currentUser ?: run {
            _errorMessage.value = "You must be logged in to create notes"
            return
        }

        val newFile = File(
            fileId = UUID.randomUUID().toString(),
            courseId = courseId,
            name = title,
            content = content,
            createdAt = System.currentTimeMillis(),
            createdBy = currentUser.uid
        )

        db.collection("files")
            .document(newFile.fileId)
            .set(newFile)
            .addOnSuccessListener {
                // Note: We don't need to manually update the list since we're using
                // a snapshot listener in loadNotes()
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Failed to create note: ${e.message}"
            }
    }
}