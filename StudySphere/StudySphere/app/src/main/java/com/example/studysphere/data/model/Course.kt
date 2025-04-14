package com.example.studysphere.data.model

data class Course(
    val courseId: String = "",
    val courseName: String = "",
    val admin: String = "",
    val members: List<String> = emptyList()
)