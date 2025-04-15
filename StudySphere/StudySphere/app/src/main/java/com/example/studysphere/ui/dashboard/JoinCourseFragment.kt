// JoinCourseFragment.kt
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
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore


class JoinCourseFragment : Fragment() {

    private lateinit var courseIdInput: TextInputEditText
    private lateinit var joinButton: Button
    private lateinit var progressBar: ProgressBar
    private lateinit var viewModel: DashboardViewModel

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_join_course, container, false)

        courseIdInput = view.findViewById(R.id.course_id_input)
        joinButton = view.findViewById(R.id.join_button)
        progressBar = view.findViewById(R.id.progress_bar)

        viewModel = ViewModelProvider(requireActivity())[DashboardViewModel::class.java]

        setupClickListeners()

        return view
    }

    private fun setupClickListeners() {
        joinButton.setOnClickListener {
            val courseId = courseIdInput.text.toString().trim()

            if (courseId.isEmpty()) {
                courseIdInput.error = "Course ID can't be empty"
                return@setOnClickListener
            }

            joinCourse(courseId)
        }
    }

    private fun joinCourse(courseId: String) {
        val currentUser = auth.currentUser ?: return
        progressBar.visibility = View.VISIBLE
        joinButton.isEnabled = false

        // Check if the course exists
        db.collection("courses").document(courseId).get()
            .addOnSuccessListener { document ->
                if (document.exists()) {
                    // Check if the user is already a member
                    val members = document.get("members") as? List<String> ?: listOf()

                    if (members.contains(currentUser.uid)) {
                        progressBar.visibility = View.GONE
                        joinButton.isEnabled = true
                        Toast.makeText(context, "You are already a member of this course", Toast.LENGTH_SHORT).show()
                        return@addOnSuccessListener
                    }

                    // Add user to course members
                    db.collection("courses").document(courseId)
                        .update("members", FieldValue.arrayUnion(currentUser.uid))
                        .addOnSuccessListener {
                            // Add course to user's enrolled courses
                            db.collection("users").document(currentUser.uid)
                                .update("enrolledCourses", FieldValue.arrayUnion(courseId))
                                .addOnSuccessListener {
                                    progressBar.visibility = View.GONE
                                    joinButton.isEnabled = true
                                    courseIdInput.text?.clear()
                                    Toast.makeText(context, "Successfully joined the course", Toast.LENGTH_SHORT).show()

                                    // Refresh the courses list
                                    viewModel.loadCourses()

                                    // Switch to the Courses tab
                                    (parentFragment as? DashboardFragment)?.switchToCoursesTab()
                                }
                                .addOnFailureListener { e ->
                                    progressBar.visibility = View.GONE
                                    joinButton.isEnabled = true
                                    Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                                }
                        }
                        .addOnFailureListener { e ->
                            progressBar.visibility = View.GONE
                            joinButton.isEnabled = true
                            Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
                        }
                } else {
                    progressBar.visibility = View.GONE
                    joinButton.isEnabled = true
                    Toast.makeText(context, "Course not found", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                progressBar.visibility = View.GONE
                joinButton.isEnabled = true
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }
}