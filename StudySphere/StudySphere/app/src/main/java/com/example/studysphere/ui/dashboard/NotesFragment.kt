package com.example.studysphere.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.appcompat.app.AlertDialog
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studysphere.R
import com.example.studysphere.data.model.File
import com.example.studysphere.databinding.FragmentNotesBinding
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout

class NotesFragment : Fragment() {
    private var courseId: String? = null
    private var _binding: FragmentNotesBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: NotesViewModel
    private lateinit var notesAdapter: NotesAdapter

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
        _binding = FragmentNotesBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[NotesViewModel::class.java]

        // Setup RecyclerView with empty list initially
        setupRecyclerView()

        // Observe LiveData
        setupObservers()

        // Set up FAB click listener
        setupListeners()

        // Load notes for this course
        courseId?.let {
            viewModel.loadNotes(it)
        }
    }

    private fun setupRecyclerView() {
        notesAdapter = NotesAdapter(emptyList()) { file ->
            // Handle note clicked - open note viewer dialog
            showNoteViewerDialog(file)
        }

        binding.recyclerViewNotes.apply {
            layoutManager = LinearLayoutManager(context)
            adapter = notesAdapter
        }
    }

    private fun setupObservers() {
        // Observe notes list
        viewModel.notes.observe(viewLifecycleOwner) { notes ->
            notesAdapter.updateNotes(notes)

            // Show empty state if no notes
            binding.textViewEmpty.visibility = if (notes.isEmpty()) View.VISIBLE else View.GONE
        }

        // Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            if (!errorMsg.isNullOrEmpty()) {
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        // FAB click listener to create new note
        binding.fabAddNote.setOnClickListener {
            showCreateNoteDialog()
        }
    }

    private fun showCreateNoteDialog() {
        val dialogView = layoutInflater.inflate(R.layout.fragment_create_note, null)
        val titleEditText = dialogView.findViewById<TextInputEditText>(R.id.editTextNoteTitle)
        val contentEditText = dialogView.findViewById<TextInputEditText>(R.id.editTextNoteContent)

        AlertDialog.Builder(requireContext())
            .setTitle("Create New Note")
            .setView(dialogView)
            .setPositiveButton("Save") { _, _ ->
                val title = titleEditText.text.toString().trim()
                val content = contentEditText.text.toString().trim()

                if (title.isNotEmpty() && courseId != null) {
                    viewModel.createNote(title, content, courseId!!)
                }
            }
            .setNegativeButton("Cancel", null)
            .show()
    }

    private fun showNoteViewerDialog(file: File) {
        val dialogView = layoutInflater.inflate(R.layout.fragment_view_note, null)
        val titleTextView = dialogView.findViewById<TextInputLayout>(R.id.textViewNoteTitle)
        val contentTextView = dialogView.findViewById<TextInputLayout>(R.id.textViewNoteContent)

        // Set the note data
        titleTextView.hint = file.name
        contentTextView.hint = file.content ?: "No content"

        AlertDialog.Builder(requireContext())
            .setTitle(file.name)
            .setView(dialogView)
            .setPositiveButton("Close", null)
            .show()
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(courseId: String?): NotesFragment {
            val fragment = NotesFragment()
            val args = Bundle()
            args.putString("courseId", courseId)
            fragment.arguments = args
            return fragment
        }
    }
}