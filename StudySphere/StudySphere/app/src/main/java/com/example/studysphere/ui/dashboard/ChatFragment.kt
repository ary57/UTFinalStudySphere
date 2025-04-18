package com.example.studysphere.ui.dashboard

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.recyclerview.widget.LinearLayoutManager
import com.example.studysphere.R
import com.example.studysphere.databinding.FragmentChatBinding
import com.example.studysphere.ui.course.ChatMessageAdapter
import com.example.studysphere.ui.course.ChatViewModel
import com.google.firebase.auth.FirebaseAuth

class ChatFragment : Fragment() {
    private var courseId: String? = null
    private var _binding: FragmentChatBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: ChatViewModel
    private lateinit var chatAdapter: ChatMessageAdapter

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
        _binding = FragmentChatBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        // Initialize ViewModel
        viewModel = ViewModelProvider(this)[ChatViewModel::class.java]

        // Setup RecyclerView and adapter
        setupRecyclerView()

        // Observe LiveData
        setupObservers()

        // Set up click listeners
        setupListeners()

        // Load messages for this course
        courseId?.let {
            viewModel.loadMessages(it)
        }
    }

    private fun setupRecyclerView() {
        // Get current user ID for the adapter
        val currentUserId = FirebaseAuth.getInstance().currentUser?.uid ?: ""

        // Initialize adapter
        chatAdapter = ChatMessageAdapter(currentUserId)

        // Set up RecyclerView
        binding.messagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context).apply {
                // Stack from end to show newest messages at the bottom
                stackFromEnd = true
            }
            adapter = chatAdapter
        }
    }

    private fun setupObservers() {
        // Observe messages
        viewModel.messages.observe(viewLifecycleOwner) { messages ->
            chatAdapter.submitList(messages)

            // Scroll to bottom on new messages
            if (messages.isNotEmpty()) {
                binding.messagesRecyclerView.scrollToPosition(messages.size - 1)
            }
        }

        // Observe loading state
        viewModel.isLoading.observe(viewLifecycleOwner) { isLoading ->
            binding.progressBar.visibility = if (isLoading) View.VISIBLE else View.GONE
        }

        // Observe error messages
        viewModel.errorMessage.observe(viewLifecycleOwner) { errorMsg ->
            if (!errorMsg.isNullOrEmpty()) {
                Toast.makeText(context, errorMsg, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun setupListeners() {
        // Send button click listener
        binding.sendButton.setOnClickListener {
            val messageText = binding.messageInput.text.toString().trim()
            if (messageText.isNotEmpty() && courseId != null) {
                viewModel.sendMessage(courseId!!, messageText)
                binding.messageInput.text?.clear()
            }
        }
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }

    companion object {
        fun newInstance(courseId: String?): ChatFragment {
            val fragment = ChatFragment()
            val args = Bundle()
            args.putString("courseId", courseId)
            fragment.arguments = args
            return fragment
        }
    }
}