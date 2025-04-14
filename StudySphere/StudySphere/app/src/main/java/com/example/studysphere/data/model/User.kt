package com.example.studysphere.data.model

data class User(
    val userId: String = "",
    val email: String = "",
    val enrolledCourses: List<String> = emptyList()
)