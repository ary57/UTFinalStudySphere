package com.example.studysphere.ui.dashboard

import androidx.swiperefreshlayout.widget.SwipeRefreshLayout
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

    private lateinit var swipeRefreshLayout: SwipeRefreshLayout

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_courses, container, false)

        swipeRefreshLayout = view.findViewById(R.id.swipe_refresh)
        recyclerView = view.findViewById(R.id.courses_recycler_view)
        progressBar = view.findViewById(R.id.progress_bar)

        viewModel = ViewModelProvider(requireActivity())[DashboardViewModel::class.java]

        setupRecyclerView()
        setupSwipeRefresh()
        observeViewModel()

        return view
    }

    private fun setupSwipeRefresh() {
        swipeRefreshLayout.setOnRefreshListener {
            viewModel.loadCourses()
        }

        // Set colors for the refresh animation
        swipeRefreshLayout.setColorSchemeResources(
            R.color.purple_500,
            R.color.purple_700,
            R.color.teal_200
        )
    }

    private fun setupRecyclerView() {
        courseAdapter = CourseAdapter(coursesList) { course ->
            // Navigate to course screen using Bundle
            val bundle = Bundle().apply {
                putString("courseId", course.courseId)
            }
            // Navigate to course fragment
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
            swipeRefreshLayout.isRefreshing = false
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            progressBar.visibility = if (isLoading && !swipeRefreshLayout.isRefreshing) View.VISIBLE else View.GONE
        }

        viewModel.error.observe(viewLifecycleOwner) { errorMessage ->
            Toast.makeText(context, errorMessage, Toast.LENGTH_SHORT).show()
            swipeRefreshLayout.isRefreshing = false
        }
    }
}