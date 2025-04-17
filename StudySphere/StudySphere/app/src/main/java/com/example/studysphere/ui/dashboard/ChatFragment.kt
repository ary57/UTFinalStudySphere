package com.example.studysphere.ui.course

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.example.studysphere.R
import com.example.studysphere.data.model.ChatMessage
import com.google.android.material.button.MaterialButton
import com.google.android.material.textfield.TextInputEditText
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Query
import java.text.SimpleDateFormat
import java.util.*

class ChatFragment : Fragment() {
    private lateinit var messagesRecyclerView: RecyclerView
    private lateinit var messageInput: TextInputEditText
    private lateinit var sendButton: MaterialButton
    private lateinit var progressBar: View

    private lateinit var chatAdapter: ChatAdapter
    private val messagesList = mutableListOf<ChatMessage>()

    private val db = FirebaseFirestore.getInstance()
    private val auth = FirebaseAuth.getInstance()

    private var courseId: String? = null

    companion object {
        fun newInstance(courseId: String?): ChatFragment {
            val fragment = ChatFragment()
            val args = Bundle()
            args.putString("courseId", courseId)
            fragment.arguments = args
            return fragment
        }
    }

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
        val view = inflater.inflate(R.layout.fragment_chat, container, false)

        // Initialize views
        messagesRecyclerView = view.findViewById(R.id.messages_recycler_view)
        messageInput = view.findViewById(R.id.message_input)
        sendButton = view.findViewById(R.id.send_button)
        progressBar = view.findViewById(R.id.progress_bar)

        // Set up RecyclerView
        setupRecyclerView()

        // Set up click listeners
        setupClickListeners()

        // Load messages
        loadMessages()

        return view
    }

    private fun setupRecyclerView() {
        chatAdapter = ChatAdapter(messagesList, auth.currentUser?.uid ?: "")
        messagesRecyclerView.apply {
            layoutManager = LinearLayoutManager(context).apply {
                stackFromEnd = true // Display messages from bottom to top
            }
            adapter = chatAdapter
        }
    }

    private fun setupClickListeners() {
        sendButton.setOnClickListener {
            val messageText = messageInput.text.toString().trim()

            if (messageText.isNotEmpty() && courseId != null) {
                sendMessage(messageText)
            }
        }
    }

    private fun sendMessage(messageText: String) {
        val currentUser = auth.currentUser ?: return

        // Disable send button and show progress
        sendButton.isEnabled = false
        progressBar.visibility = View.VISIBLE

        // Create message ID
        val messageId = db.collection("messages").document().id

        // Get user display name
        val senderName = currentUser.displayName ?: currentUser.email ?: "Unknown User"

        // Create message object
        val message = ChatMessage(
            messageId = messageId,
            courseId = courseId ?: "",
            senderId = currentUser.uid,
            content = messageText,
            senderName = senderName
        )

        // Save to Firestore
        db.collection("courses")
            .document(courseId ?: "")
            .collection("messages")
            .document(messageId)
            .set(message)
            .addOnSuccessListener {
                // Clear input and enable send button
                messageInput.text?.clear()
                sendButton.isEnabled = true
                progressBar.visibility = View.GONE

                // Scroll to the bottom
                messagesRecyclerView.scrollToPosition(messagesList.size - 1)
            }
            .addOnFailureListener { e ->
                sendButton.isEnabled = true
                progressBar.visibility = View.GONE
                Toast.makeText(context, "Error: ${e.message}", Toast.LENGTH_SHORT).show()
            }
    }

    private fun loadMessages() {
        courseId?.let { id ->
            progressBar.visibility = View.VISIBLE

            db.collection("courses")
                .document(id)
                .collection("messages")
                .orderBy("timestamp", Query.Direction.ASCENDING)
                .addSnapshotListener { snapshot, e ->
                    if (e != null) {
                        progressBar.visibility = View.GONE
                        Toast.makeText(context, "Error loading messages: ${e.message}", Toast.LENGTH_SHORT).show()
                        return@addSnapshotListener
                    }

                    messagesList.clear()

                    if (snapshot != null) {
                        for (doc in snapshot.documents) {
                            val message = doc.toObject(ChatMessage::class.java)
                            message?.let { messagesList.add(it) }
                        }

                        chatAdapter.notifyDataSetChanged()

                        // Scroll to the bottom if there are messages
                        if (messagesList.isNotEmpty()) {
                            messagesRecyclerView.scrollToPosition(messagesList.size - 1)
                        }
                    }

                    progressBar.visibility = View.GONE
                }
        }
    }
}

class ChatAdapter(
    private val messages: List<ChatMessage>,
    private val currentUserId: String
) : RecyclerView.Adapter<ChatViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ChatViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return ChatViewHolder(view)
    }

    override fun onBindViewHolder(holder: ChatViewHolder, position: Int) {
        holder.bind(messages[position])
    }

    override fun getItemCount() = messages.size
}

class ChatViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
    private val senderName: TextView = itemView.findViewById(R.id.sender_name)
    private val messageTimestamp: TextView = itemView.findViewById(R.id.message_timestamp)
    private val messageContent: TextView = itemView.findViewById(R.id.message_content)

    fun bind(message: ChatMessage) {
        senderName.text = message.senderName
        messageContent.text = message.content

        // Format timestamp
        message.timestamp?.let {
            val sdf = SimpleDateFormat("hh:mm a", Locale.getDefault())
            val date = it.toDate()
            messageTimestamp.text = sdf.format(date)
        }
    }
}