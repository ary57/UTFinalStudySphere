package com.example.studysphere.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.DividerItemDecoration
import com.example.studysphere.databinding.FragmentMeetsBinding
import com.example.studysphere.ui.course.CourseScreenFragment
import com.example.studysphere.ui.course.CourseScreenFragmentDirections

class MeetsFragment : Fragment() {

    private var _binding: FragmentMeetsBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MeetsViewModel
    private lateinit var sessionAdapter: StudySessionAdapter

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
    ): View {
        _binding = FragmentMeetsBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[MeetsViewModel::class.java]

        // If courseId is null, try to get it from parent fragment (CourseScreenFragment)
        if (courseId == null) {
            val parentFragment = parentFragment
            if (parentFragment is CourseScreenFragment) {
                courseId = parentFragment.getCourseId()
            }
        }

        if (courseId == null) {
            Toast.makeText(context, "Error: Course ID not found", Toast.LENGTH_SHORT).show()
            return
        }

        setupRecyclerView()
        setupObservers()
        setupListeners()

        viewModel.loadStudySessions(courseId!!)
    }

    private fun setupRecyclerView() {
        sessionAdapter = StudySessionAdapter(
            onRsvpClick = { sessionId, status ->
                viewModel.updateRsvpStatus(
                    sessionId,
                    status,
                    onSuccess = {
                        // RSVP update successful
                    },
                    onFailure = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        )

        binding.recyclerViewSessions.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = sessionAdapter
            addItemDecoration(DividerItemDecoration(context, DividerItemDecoration.VERTICAL))
        }
    }

    private fun setupObservers() {
        viewModel.studySessions.observe(viewLifecycleOwner) { sessions ->
            sessionAdapter.submitList(sessions)
            binding.textViewEmpty.visibility = if (sessions.isEmpty()) View.VISIBLE else View.GONE
        }

        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            errorMsg?.let {
                Toast.makeText(context, it, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        binding.buttonCreateSession.setOnClickListener {
            courseId?.let { id ->
                // Use the generated Directions class
                val action = CourseScreenFragmentDirections.actionCourseScreenFragmentToCreateSessionFragment(id)
                findNavController().navigate(action)
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(courseId: String?): MeetsFragment {
            val fragment = MeetsFragment()
            val args = Bundle()
            args.putString("courseId", courseId)
            fragment.arguments = args
            return fragment
        }
    }
}