package com.example.studysphere.ui.course

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.studysphere.data.model.ChatMessage
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import com.google.firebase.firestore.ListenerRegistration

class ChatViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _messages = MutableLiveData<List<ChatMessage>>()
    val messages: LiveData<List<ChatMessage>> = _messages

    private val _errorMessage = MutableLiveData<String>()
    val errorMessage: LiveData<String> = _errorMessage

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private var messageListener: ListenerRegistration? = null

    // Get current user ID
    val currentUserId: String
        get() = auth.currentUser?.uid ?: ""

    // Get current user name
    private val currentUserName: String
        get() = auth.currentUser?.displayName ?: auth.currentUser?.email ?: "Unknown User"

    fun loadMessages(courseId: String) {
        _isLoading.value = true

        // Clean up previous listener if exists
        messageListener?.remove()

        // Create new listener
        messageListener = db.collection("courses")
            .document(courseId)
            .collection("messages")
            .orderBy("timestamp", Query.Direction.ASCENDING)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _errorMessage.value = "Failed to load messages: ${e.message}"
                    _isLoading.value = false
                    return@addSnapshotListener
                }

                val messageList = mutableListOf<ChatMessage>()

                if (snapshot != null) {
                    for (doc in snapshot.documents) {
                        val message = doc.toObject(ChatMessage::class.java)
                        message?.let { messageList.add(it) }
                    }
                }

                _messages.value = messageList
                _isLoading.value = false
            }
    }

    fun sendMessage(courseId: String, content: String) {
        if (content.trim().isEmpty()) {
            return
        }

        _isLoading.value = true

        // Generate unique ID for the message
        val messageId = db.collection("messages").document().id

        // Create message object
        val message = ChatMessage(
            messageId = messageId,
            courseId = courseId,
            senderId = currentUserId,
            content = content,
            senderName = currentUserName,
            type = "text"
        )

        // Save to Firestore
        db.collection("courses")
            .document(courseId)
            .collection("messages")
            .document(messageId)
            .set(message)
            .addOnSuccessListener {
                _isLoading.value = false
            }
            .addOnFailureListener { e ->
                _errorMessage.value = "Failed to send message: ${e.message}"
                _isLoading.value = false
            }
    }

    override fun onCleared() {
        super.onCleared()
        // Remove listener when ViewModel is cleared
        messageListener?.remove()
    }
}