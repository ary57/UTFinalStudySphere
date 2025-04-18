package com.example.studysphere.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId

data class StudySession(
    @DocumentId
    val sessionId: String = "",
    val courseId: String = "",
    val title: String = "",
    val date: Timestamp = Timestamp.now(),
    val time: String = "",
    val location: String = "",
    val description: String = "",
    val attendees: Int = 0,
    val rsvps: Map<String, String> = mapOf()
) {
    companion object {
        const val RSVP_YES = "yes"
        const val RSVP_NO = "no"
        const val RSVP_MAYBE = "maybe"
    }
}