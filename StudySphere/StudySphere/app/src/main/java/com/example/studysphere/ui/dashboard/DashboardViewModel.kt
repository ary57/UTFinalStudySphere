package com.example.studysphere.ui.dashboard

import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.studysphere.data.model.Course
import com.example.studysphere.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class DashboardViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _courses = MutableLiveData<List<Course>>()
    val courses: LiveData<List<Course>> = _courses

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

    // Add a loading LiveData
    private val _loading = MutableLiveData<Boolean>()
    val loading: LiveData<Boolean> = _loading

    // User data
    private val _user = MutableLiveData<User?>()
    val user: LiveData<User?> = _user

    init {
        // Load user data on initialization
        loadUserData()
    }

    fun loadUserData() {
        val currentUser = auth.currentUser ?: return

        db.collection("users").document(currentUser.uid).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    val userData = document.toObject(User::class.java)
                    _user.value = userData
                    loadCourses()
                } else {
                    // If user document doesn't exist, create it
                    val newUser = User(
                        userId = currentUser.uid,
                        email = currentUser.email ?: "",
                        enrolledCourses = listOf()
                    )

                    db.collection("users").document(currentUser.uid).set(newUser)
                        .addOnSuccessListener {
                            _user.value = newUser
                        }
                        .addOnFailureListener { e ->
                            _error.value = "Failed to create user: ${e.message}"
                        }
                }
            }
            .addOnFailureListener { e ->
                _error.value = "Failed to load user data: ${e.message}"
            }
    }

    fun loadCourses() {
        _isLoading.value = true

        val currentUser = auth.currentUser
        if (currentUser != null) {
            db.collection("users").document(currentUser.uid).get()
                .addOnSuccessListener { document ->
                    if (document != null) {
                        val user = document.toObject(User::class.java)
                        user?.enrolledCourses?.let { courseIds ->
                            if (courseIds.isEmpty()) {
                                _courses.value = emptyList()
                                _isLoading.value = false
                                return@let
                            }

                            db.collection("courses")
                                .whereIn("courseId", courseIds)
                                .get()
                                .addOnSuccessListener { documents ->
                                    val coursesList = mutableListOf<Course>()
                                    for (doc in documents) {
                                        val course = doc.toObject(Course::class.java)
                                        coursesList.add(course)
                                    }
                                    _courses.value = coursesList
                                    _isLoading.value = false
                                }
                                .addOnFailureListener { e ->
                                    _error.value = "Failed to load courses: ${e.message}"
                                    _isLoading.value = false
                                }
                        }
                    }
                }
                .addOnFailureListener { e ->
                    _error.value = "Failed to load user data: ${e.message}"
                    _isLoading.value = false
                }
        }
    }
    private var coursesListener: ListenerRegistration? = null

    fun startListeningForCourseUpdates() {
        val currentUser = auth.currentUser ?: return

        db.collection("users").document(currentUser.uid)
            .addSnapshotListener { snapshot, e ->
                if (e != null) {
                    _error.value = "Failed to listen for user updates: ${e.message}"
                    return@addSnapshotListener
                }

                if (snapshot != null && snapshot.exists()) {
                    val user = snapshot.toObject(User::class.java)
                    _user.value = user
                    loadCourses()
                }
            }
    }

    fun stopListeningForCourseUpdates() {
        coursesListener?.remove()
    }

    override fun onCleared() {
        super.onCleared()
        stopListeningForCourseUpdates()
    }

    fun createCourse(courseName: String, onSuccess: () -> Unit, onFailure: (String) -> Unit) {
        _loading.value = true

        val currentUser = auth.currentUser ?: run {
            onFailure("User not authenticated")
            _loading.value = false
            return
        }

        // Generate a unique courseId
        val courseId = db.collection("courses").document().id

        val newCourse = Course(
            courseId = courseId,
            courseName = courseName,
            admin = currentUser.uid,
            members = listOf(currentUser.uid)
        )

        // Add course to Firestore
        db.collection("courses").document(courseId).set(newCourse)
            .addOnSuccessListener {
                // Update user's enrolled courses
                db.collection("users").document(currentUser.uid)
                    .update("enrolledCourses", com.google.firebase.firestore.FieldValue.arrayUnion(courseId))
                    .addOnSuccessListener {
                        _loading.value = false
                        loadCourses() // Refresh courses
                        onSuccess()
                    }
                    .addOnFailureListener { e ->
                        _loading.value = false
                        onFailure("Failed to update user: ${e.message}")
                    }
            }
            .addOnFailureListener { e ->
                _loading.value = false
                onFailure("Failed to create course: ${e.message}")
            }
    }

    fun joinCourse(courseId: String, onSuccess: () -> Unit, onFailure: (String, Any?) -> Unit) {
        _loading.value = true

        val currentUser = auth.currentUser ?: run {
            onFailure("User not authenticated", null)
            _loading.value = false
            return
        }

        // Check if course exists
        db.collection("courses").document(courseId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Check if user is already a member
                    val course = document.toObject(Course::class.java)
                    if (course?.members?.contains(currentUser.uid) == true) {
                        _loading.value = false
                        onFailure("You are already a member of this course", null)
                        return@addOnSuccessListener
                    }

                    // Add user to course members
                    db.collection("courses").document(courseId)
                        .update("members", com.google.firebase.firestore.FieldValue.arrayUnion(currentUser.uid))
                        .addOnSuccessListener {
                            // Add course to user's enrolled courses
                            db.collection("users").document(currentUser.uid)
                                .update("enrolledCourses", com.google.firebase.firestore.FieldValue.arrayUnion(courseId))
                                .addOnSuccessListener {
                                    _loading.value = false
                                    loadCourses() // Refresh courses
                                    onSuccess()
                                }
                                .addOnFailureListener { e ->
                                    _loading.value = false
                                    onFailure("Failed to update user: ${e.message}", null)
                                }
                        }
                        .addOnFailureListener { e ->
                            _loading.value = false
                            onFailure("Failed to join course: ${e.message}", null)
                        }
                } else {
                    _loading.value = false
                    onFailure("Course not found", null)
                }
            }
            .addOnFailureListener { e ->
                _loading.value = false
                onFailure("Failed to check course: ${e.message}", null)
            }
    }
}