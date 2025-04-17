package com.example.studysphere.ui.course

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.studysphere.R
import com.google.firebase.firestore.FirebaseFirestore

class CourseFragment : Fragment() {

    private var courseId: String? = null
    private val db = FirebaseFirestore.getInstance()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            courseId = it.getString("courseId")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        // This is a transitional fragment that will navigate to CourseScreenFragment
        // once we retrieve the course name

        // Get course details from Firestore
        courseId?.let { id ->
            db.collection("courses").document(id).get()
                .addOnSuccessListener { document ->
                    if (document != null && document.exists()) {
                        val courseName = document.getString("courseName") ?: "Course"

                        // Navigate to CourseScreenFragment
                        val bundle = Bundle().apply {
                            putString("courseId", courseId)
                            putString("courseName", courseName)
                        }
                        findNavController().navigate(R.id.courseScreenFragment, bundle)
                    }
                }
        }

        // Return a simple loading view while we get the course details
        return inflater.inflate(R.layout.fragment_course, container, false)
    }
}