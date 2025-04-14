import androidx.lifecycle.LiveData
import androidx.lifecycle.MutableLiveData
import androidx.lifecycle.ViewModel
import com.example.studysphere.data.model.Course
import com.example.studysphere.data.model.User
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class DashboardViewModel : ViewModel() {
    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private val _courses = MutableLiveData<List<Course>>()
    val courses: LiveData<List<Course>> = _courses

    private val _isLoading = MutableLiveData<Boolean>()
    val isLoading: LiveData<Boolean> = _isLoading

    private val _error = MutableLiveData<String>()
    val error: LiveData<String> = _error

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
                                .addOnFailureListener {
                                    _error.value = "Failed to load courses"
                                    _isLoading.value = false
                                }
                        }
                    }
                }
                .addOnFailureListener {
                    _error.value = "Failed to load user data"
                    _isLoading.value = false
                }
        }
    }
}