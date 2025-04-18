package com.example.studysphere.ui.course

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.Fragment
import androidx.viewpager2.adapter.FragmentStateAdapter
import androidx.viewpager2.widget.ViewPager2
import com.example.studysphere.R
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.bottomnavigation.BottomNavigationView

class CourseScreenFragment : Fragment() {

    private lateinit var viewPager: ViewPager2
    private lateinit var bottomNavigation: BottomNavigationView
    private lateinit var toolbar: MaterialToolbar
    private var courseId: String? = null
    private var courseName: String? = null

    // Add imports for the dashboard fragments
    private val chatFragmentClass = com.example.studysphere.ui.dashboard.ChatFragment::class.java
    private val notesFragmentClass = com.example.studysphere.ui.dashboard.NotesFragment::class.java
    private val meetsFragmentClass = com.example.studysphere.ui.dashboard.MeetsFragment::class.java

    // Add public getter method for courseId
    fun getCourseId(): String? {
        return courseId
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        arguments?.let {
            courseId = it.getString("courseId")
            courseName = it.getString("courseName")
        }
    }

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.course_screen, container, false)

        // Initialize views
        viewPager = view.findViewById(R.id.viewPager)
        bottomNavigation = view.findViewById(R.id.bottom_navigation)
        toolbar = view.findViewById(R.id.toolbar)

        // Set course name in toolbar
        courseName?.let {
            toolbar.title = it
        }

        // Set up ViewPager
        setupViewPager()

        // Set up bottom navigation
        setupBottomNavigation()

        return view
    }

    private fun setupViewPager() {
        val adapter = CourseTabsAdapter(this)
        viewPager.adapter = adapter

        // Handle page changes
        viewPager.registerOnPageChangeCallback(object : ViewPager2.OnPageChangeCallback() {
            override fun onPageSelected(position: Int) {
                when (position) {
                    0 -> bottomNavigation.selectedItemId = R.id.nav_chat
                    1 -> bottomNavigation.selectedItemId = R.id.nav_notes
                    2 -> bottomNavigation.selectedItemId = R.id.nav_meets
                }
            }
        })
    }

    private fun setupBottomNavigation() {
        bottomNavigation.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_chat -> {
                    viewPager.currentItem = 0
                    true
                }
                R.id.nav_notes -> {
                    viewPager.currentItem = 1
                    true
                }
                R.id.nav_meets -> {
                    viewPager.currentItem = 2
                    true
                }
                else -> false
            }
        }
    }

    inner class CourseTabsAdapter(fragment: Fragment) : FragmentStateAdapter(fragment) {
        override fun getItemCount(): Int = 3

        override fun createFragment(position: Int): Fragment {
            return when (position) {
                0 -> com.example.studysphere.ui.dashboard.ChatFragment.newInstance(courseId)
                1 -> com.example.studysphere.ui.dashboard.NotesFragment.newInstance(courseId)
                2 -> com.example.studysphere.ui.dashboard.MeetsFragment.newInstance(courseId)
                else -> throw IllegalArgumentException("Invalid position $position")
            }
        }
    }
}