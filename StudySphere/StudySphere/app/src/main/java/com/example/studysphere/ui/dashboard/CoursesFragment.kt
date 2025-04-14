package com.example.studysphere.ui.dashboard

import DashboardViewModel
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studysphere.R
import com.example.studysphere.data.model.Course
import androidx.navigation.fragment.findNavController

class CoursesFragment : Fragment() {

    private lateinit var recyclerView: RecyclerView
    private lateinit var progressBar: ProgressBar
    private lateinit var courseAdapter: CourseAdapter
    private val coursesList = mutableListOf<Course>()

    private lateinit var viewModel: DashboardViewModel

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_courses, container, false)
        recyclerView = view.findViewById(R.id.courses_recycler_view)
        progressBar = view.findViewById(R.id.progress_bar)

        viewModel = ViewModelProvider(requireActivity())[DashboardViewModel::class.java]

        setupRecyclerView()
        observeViewModel()
        viewModel.loadCourses()

        return view
    }

    private fun setupRecyclerView() {
        courseAdapter = CourseAdapter(coursesList) { course ->
            // Navigate to course screen using Bundle instead of SafeArgs
            val bundle = Bundle().apply {
                putString("courseId", course.courseId)
            }
            // Use courseFragment instead of R.id.courseFragment
            findNavController().navigate(R.id.courseFragment, bundle)
        }

        recyclerView.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = courseAdapter
        }
    }

    private fun observeViewModel() {
        viewModel.courses.observe(viewLifecycleOwner) { courses ->
            coursesList.clear()
            coursesList.addAll(courses)
            courseAdapter.notifyDataSetChanged()
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
        }
    }
}