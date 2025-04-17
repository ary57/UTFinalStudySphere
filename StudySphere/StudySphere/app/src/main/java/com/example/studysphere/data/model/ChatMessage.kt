package com.example.studysphere.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.ServerTimestamp

data class ChatMessage(
    val messageId: String = "",
    val courseId: String = "",
    val senderId: String = "",
    @ServerTimestamp
    val timestamp: Timestamp? = null,
    val content: String = "",
    val senderName: String = "",
    val type: String = "text" // For future: "text", "image", "pdf"
) {
    // Empty constructor for Firebase
    constructor() : this("", "", "", null, "", "", "text")
}