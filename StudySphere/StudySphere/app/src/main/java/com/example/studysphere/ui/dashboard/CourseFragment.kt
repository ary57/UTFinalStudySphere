package com.example.studysphere.ui.course

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.fragment.app.Fragment
import com.example.studysphere.R

class CourseFragment : Fragment() {

    private var courseId: String? = null

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
        // For now, just create a simple layout with a TextView
        val view = inflater.inflate(R.layout.fragment_course, container, false)
        val textView = view.findViewById<TextView>(R.id.course_id_text)
        textView.text = "Course ID: $courseId"
        return view
    }
}