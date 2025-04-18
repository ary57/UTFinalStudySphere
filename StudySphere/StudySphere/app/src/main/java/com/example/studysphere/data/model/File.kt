// File.kt
package com.example.studysphere.data.model

data class File(
    val fileId: String = "",
    val courseId: String = "",
    val name: String = "",
    val content: String = "",
    val createdAt: Long = System.currentTimeMillis(),
    val createdBy: String = ""
) {
    // Empty constructor for Firestore
    constructor() : this("", "", "", "", 0L, "")
}