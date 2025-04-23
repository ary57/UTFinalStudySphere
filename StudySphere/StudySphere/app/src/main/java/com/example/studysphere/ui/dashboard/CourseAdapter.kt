package com.example.studysphere.ui.dashboard

import android.content.ClipData
import android.content.ClipboardManager
import android.content.Context
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageButton
import android.widget.TextView
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.example.studysphere.R
import com.example.studysphere.data.model.Course

class CourseAdapter(
    private var courses: List<Course>,
    private val onCourseClicked: (Course) -> Unit
) : RecyclerView.Adapter<CourseAdapter.CourseViewHolder>() {

    // Add updateCourses method to support DashboardViewModel functionality
    fun updateCourses(newCourses: List<Course>) {
        courses = newCourses
        notifyDataSetChanged()
    }

    class CourseViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        val courseName: TextView = itemView.findViewById(R.id.course_name)
        val copyButton: ImageButton = itemView.findViewById(R.id.copy_course_id_button)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): CourseViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_course, parent, false)
        return CourseViewHolder(view)
    }

    override fun onBindViewHolder(holder: CourseViewHolder, position: Int) {
        val course = courses[position]
        holder.courseName.text = course.courseName

        // Set click listener for the course item
        holder.itemView.setOnClickListener {
            onCourseClicked(course)
        }

        // Set click listener for the copy button
        holder.copyButton.setOnClickListener {
            copyCourseIdToClipboard(it.context, course.courseId)
        }
    }

    private fun copyCourseIdToClipboard(context: Context, courseId: String) {
        val clipboard = context.getSystemService(Context.CLIPBOARD_SERVICE) as ClipboardManager
        val clip = ClipData.newPlainText("Course ID", courseId)
        clipboard.setPrimaryClip(clip)
        Toast.makeText(context, "Course ID copied to clipboard", Toast.LENGTH_SHORT).show()
    }

    override fun getItemCount() = courses.size
}