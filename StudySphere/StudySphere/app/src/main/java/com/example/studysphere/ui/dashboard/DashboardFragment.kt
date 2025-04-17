package com.example.studysphere.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studysphere.R
import com.example.studysphere.data.model.Course
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText

class DashboardFragment : Fragment() {
    private lateinit var viewModel: DashboardViewModel
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var coursesRecyclerView: RecyclerView
    private lateinit var createCourseView: View
    private lateinit var joinCourseView: View
    private lateinit var courseAdapter: CourseAdapter

    // Create Course Views
    private lateinit var courseNameInput: TextInputEditText
    private lateinit var createCourseButton: Button

    // Join Course Views
    private lateinit var courseIdInput: TextInputEditText
    private lateinit var joinCourseButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        // Initialize ViewModel
        viewModel = ViewModelProvider(requireActivity())[DashboardViewModel::class.java]

        // Setup Views
        setupViews(view)
        setupListeners()
        observeViewModel()

        return view
    }

    private fun setupViews(view: View) {
        // Setup RecyclerView
        coursesRecyclerView = view.findViewById(R.id.courses_recycler_view)
        courseAdapter = CourseAdapter(emptyList()) { course ->
            // Navigate directly to CourseScreenFragment
            val bundle = Bundle().apply {
                putString("courseId", course.courseId)
                putString("courseName", course.courseName)
            }
            findNavController().navigate(R.id.action_dashboardFragment_to_courseScreenFragment, bundle)
        }
        coursesRecyclerView.layoutManager = LinearLayoutManager(context)
        coursesRecyclerView.adapter = courseAdapter

        // Setup Bottom Navigation
        bottomNavigation = view.findViewById(R.id.bottom_navigation)

        // Since these views are missing in your layout, we need to inflate them or add them
        // For now, we'll create placeholders or handle their absence
        try {
            createCourseView = view.findViewById(R.id.create_course_container)
            joinCourseView = view.findViewById(R.id.join_course_container)

            // Create Course Views
            courseNameInput = view.findViewById(R.id.course_name_input)
            createCourseButton = view.findViewById(R.id.create_button)

            // Join Course Views
            courseIdInput = view.findViewById(R.id.course_id_input)
            joinCourseButton = view.findViewById(R.id.join_button)
        } catch (e: Exception) {
            // Handle missing views - you'll need to add these to your layout
            Toast.makeText(requireContext(), "Some UI elements are missing", Toast.LENGTH_SHORT).show()
        }

        // Initially show courses tab
        showCoursesTab()
    }

    private fun setupListeners() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_courses -> {
                    showCoursesTab()
                    true
                }
                R.id.nav_create_course -> {
                    showCreateCourseTab()
                    true
                }
                R.id.nav_join_course -> {
                    showJoinCourseTab()
                    true
                }
                else -> false
            }
        }

        // Only set up these listeners if the views exist
        if (::createCourseButton.isInitialized) {
            createCourseButton.setOnClickListener {
                val courseName = courseNameInput.text.toString().trim()
                if (courseName.isNotEmpty()) {
                    viewModel.createCourse(
                        courseName,
                        onSuccess = {
                            courseNameInput.text?.clear()
                            switchToCoursesTab()
                        },
                        onFailure = { errorMessage ->
                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    Toast.makeText(requireContext(), "Please enter a course name", Toast.LENGTH_SHORT).show()
                }
            }
        }

        if (::joinCourseButton.isInitialized) {
            joinCourseButton.setOnClickListener {
                val courseId = courseIdInput.text.toString().trim()
                if (courseId.isNotEmpty()) {
                    viewModel.joinCourse(
                        courseId,
                        onSuccess = {
                            courseIdInput.text?.clear()
                            switchToCoursesTab()
                        },
                        onFailure = { errorMessage, _ ->
                            Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_SHORT).show()
                        }
                    )
                } else {
                    Toast.makeText(requireContext(), "Please enter a course ID", Toast.LENGTH_SHORT).show()
                }
            }
        }
    }

    private fun observeViewModel() {
        viewModel.courses.observe(viewLifecycleOwner) { courses ->
            courseAdapter.updateCourses(courses)
        }

        viewModel.loading.observe(viewLifecycleOwner) { isLoading ->
            // Show/hide loading indicator if needed
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            if (errorMessage.isNotEmpty()) {
                Toast.makeText(requireContext(), errorMessage, Toast.LENGTH_LONG).show()
            }
        }
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.startListeningForCourseUpdates()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        viewModel.stopListeningForCourseUpdates()
    }

    fun switchToCoursesTab() {
        bottomNavigation.selectedItemId = R.id.nav_courses
    }

    private fun showCoursesTab() {
        coursesRecyclerView.visibility = View.VISIBLE
        if (::createCourseView.isInitialized) createCourseView.visibility = View.GONE
        if (::joinCourseView.isInitialized) joinCourseView.visibility = View.GONE
    }

    private fun showCreateCourseTab() {
        coursesRecyclerView.visibility = View.GONE
        if (::createCourseView.isInitialized) createCourseView.visibility = View.VISIBLE
        if (::joinCourseView.isInitialized) joinCourseView.visibility = View.GONE
    }

    private fun showJoinCourseTab() {
        coursesRecyclerView.visibility = View.GONE
        if (::createCourseView.isInitialized) createCourseView.visibility = View.GONE
        if (::joinCourseView.isInitialized) joinCourseView.visibility = View.VISIBLE
    }
}