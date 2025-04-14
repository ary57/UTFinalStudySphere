package com.example.studysphere.ui.dashboard

import DashboardViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.fragment.app.viewModels
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studysphere.R
import com.example.studysphere.data.model.Course
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.textfield.TextInputEditText

class DashboardFragment : Fragment() {
    private val viewModel: DashboardViewModel by viewModels()
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var coursesRecyclerView: RecyclerView
    private lateinit var courseAdapter: CourseAdapter

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_dashboard, container, false)

        // Setup RecyclerView
        coursesRecyclerView = view.findViewById(R.id.courses_recycler_view)
        courseAdapter = CourseAdapter(emptyList()) { course ->
            // TODO: Navigate to course details
            Toast.makeText(context, "Selected: ${course.courseName}", Toast.LENGTH_SHORT).show()
        }
        coursesRecyclerView.layoutManager = LinearLayoutManager(context)
        coursesRecyclerView.adapter = courseAdapter

        // Setup Bottom Navigation
        bottomNavigation = view.findViewById(R.id.bottom_navigation)
        bottomNavigation.setOnNavigationItemSelectedListener { item ->
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

        // Observe courses
        viewModel.courses.observe(viewLifecycleOwner) { courses ->
            courseAdapter.updateCourses(courses)
        }

        // Fetch initial courses
        viewModel.loadCourses()

        return view
    }

    private fun showCoursesTab() {
        // TODO: Implement courses tab logic
    }

    private fun showCreateCourseTab() {
        // TODO: Implement create course tab logic
    }

    private fun showJoinCourseTab() {
        // TODO: Implement join course tab logic
    }
}

class CourseAdapter(
    private var courses: List<Course>,
    private val onCourseClick: (Course) -> Unit
) : RecyclerView.Adapter<CourseViewHolder>() {

    fun updateCourses(newCourses: List<Course>) {
        courses = newCourses
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view, onCourseClick)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        holder.bind(courses[position])
    }

    override fun getItemCount() = courses.size
}

class CourseViewHolder(
    itemView: View,
    private val onCourseClick: (Course) -> Unit
) : RecyclerView.ViewHolder(itemView) {
    // TODO: Add views for course item
    fun bind(course: Course) {
        // TODO: Bind course data to views
        itemView.setOnClickListener { onCourseClick(course) }
    }
}