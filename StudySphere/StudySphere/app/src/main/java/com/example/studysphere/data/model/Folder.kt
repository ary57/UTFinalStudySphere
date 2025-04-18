package com.example.studysphere.data.model

import com.google.firebase.Timestamp
import com.google.firebase.firestore.DocumentId
import com.google.firebase.firestore.ServerTimestamp

data class Folder(
    @DocumentId
    val folderId: String = "", // Unique identifier
    val courseId: String = "", // Links folder to specific course
    val name: String = "",
    val parentFolderId: String? = null, // Null for top-level folders
    @ServerTimestamp
    val timestamp: Timestamp = Timestamp.now() // For chronological sorting
) {
    // Empty constructor for Firestore
    constructor() : this("", "", "", null)

    // Check if this is a root-level folder
    fun isRootFolder(): Boolean = parentFolderId == null
}