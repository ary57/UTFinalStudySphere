package com.example.studysphere.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import com.example.studysphere.R
import com.example.studysphere.data.model.Course
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import java.util.*

class CreateCourseFragment : Fragment() {

    private lateinit var courseNameInput: TextInputEditText
    private lateinit var createButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var viewModel: DashboardViewModel

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_create_course, container, false)

        courseNameInput = view.findViewById(R.id.course_name_input)
        createButton = view.findViewById(R.id.create_button)
        progressBar = view.findViewById(R.id.progress_bar)

        viewModel = ViewModelProvider(requireActivity())[DashboardViewModel::class.java]

        setupClickListeners()

        return view
    }

    private fun setupClickListeners() {
        createButton.setOnClickListener {
            val courseName = courseNameInput.text.toString().trim()

            if (courseName.isEmpty()) {
                courseNameInput.error = "Course name can't be empty"
                return@setOnClickListener
            }

            createCourse(courseName)
        }
    }

    private fun createCourse(courseName: String) {
        val currentUser = auth.currentUser ?: return
        progressBar.visibility = View.VISIBLE
        createButton.isEnabled = false

        // Generate a unique courseId
        val courseId = UUID.randomUUID().toString()

        // Create the course object
        val course = Course(
            courseId = courseId,
            courseName = courseName,
            admin = currentUser.uid,
            members = listOf(currentUser.uid)
        )

        // Save the course to Firestore
        db.collection("courses").document(courseId).set(course)
            .addOnSuccessListener {
                // Add course to user's enrolled courses
                db.collection("users").document(currentUser.uid)
                    .get()
                    .addOnSuccessListener { document ->
                        val enrolledCourses = document.get("enrolledCourses") as? List<String> ?: listOf()
                        val updatedCourses = enrolledCourses + courseId

                        db.collection("users").document(currentUser.uid)
                            .update("enrolledCourses", updatedCourses)
                            .addOnSuccessListener {
                                progressBar.visibility = View.GONE
                                createButton.isEnabled = true
                                courseNameInput.text?.clear()
                                Toast.makeText(context, "Course created successfully", Toast.LENGTH_SHORT).show()

                                // Refresh the courses list
                                viewModel.loadCourses()

                                // Switch to the Courses tab
                                (parentFragment as? DashboardFragment)?.switchToCoursesTab()
                            }
                            .addOnFailureListener { e ->
                                progressBar.visibility = View.GONE
                                createButton.isEnabled = true
                                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        progressBar.visibility = View.GONE
                        createButton.isEnabled = true
                        Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                    }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                createButton.isEnabled = true
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}