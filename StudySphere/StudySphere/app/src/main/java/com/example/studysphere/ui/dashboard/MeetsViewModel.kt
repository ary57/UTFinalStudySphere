package com.example.studysphere.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.studysphere.data.model.StudySession
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query

class MeetsViewModel : ViewModel() {

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _studySessions = MutableLiveData<List<StudySession>>()
    val studySessions: LiveData<List<StudySession>> = _studySessions

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _errorMessage = MutableLiveData<String?>()
    val errorMessage: LiveData<String?> = _errorMessage

    private var sessionsListener: ListenerRegistration? = null

    fun loadStudySessions(courseId: String) {
        _isLoading.value = true
        _errorMessage.value = null

        sessionsListener = db.collection("studySessions")
            .whereEqualTo("courseId", courseId)
            .orderBy("date", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, exception ->
                if (exception != null) {
                    _errorMessage.value = "Failed to load study sessions: ${exception.message}"
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                val sessionsList = snapshot?.documents?.mapNotNull { doc ->
                    doc.toObject(StudySession::class.java)
                } ?: listOf()

                _studySessions.value = sessionsList
                _isLoading.value = false
            }
    }

    fun createStudySession(session: StudySession, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        _isLoading.value = true

        val sessionMap = hashMapOf(
            "courseId" to session.courseId,
            "title" to session.title,
            "date" to session.date,
            "time" to session.time,
            "location" to session.location,
            "description" to session.description,
            "attendees" to 0,
            "rsvps" to mapOf<String, String>()
        )

        db.collection("studySessions")
            .add(sessionMap)
            .addOnSuccessListener {
                _isLoading.value = false
                onSuccess()
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                onFailure("Failed to create study session: ${e.message}")
            }
    }

    fun updateRsvpStatus(sessionId: String, status: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        _isLoading.value = true

        val currentUserId = auth.currentUser?.uid ?: return

        db.collection("studySessions").document(sessionId)
            .update("rsvps.$currentUserId", status)
            .addOnSuccessListener {
                _isLoading.value = false
                onSuccess()
            }
            .addOnFailureListener { e ->
                _isLoading.value = false
                onFailure("Failed to update RSVP: ${e.message}")
            }
    }

    override fun onCleared() {
        super.onCleared()
        sessionsListener?.remove()
    }
}