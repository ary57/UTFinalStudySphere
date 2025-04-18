package com.example.studysphere.ui.course

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import androidx.constraintlayout.widget.ConstraintSet
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studysphere.R
import com.example.studysphere.data.model.ChatMessage
import com.google.android.material.card.MaterialCardView
import java.text.SimpleDateFormat
import java.util.*

class ChatMessageAdapter(private val currentUserId: String) :
    ListAdapter<ChatMessage, ChatMessageAdapter.MessageViewHolder>(MessageDiffCallback()) {

    private val dateFormat = SimpleDateFormat("h:mm a", Locale.getDefault())

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): MessageViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_chat_message, parent, false)
        return MessageViewHolder(view)
    }

    override fun onBindViewHolder(holder: MessageViewHolder, position: Int) {
        val message = getItem(position)
        holder.bind(message)
    }

    inner class MessageViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val senderName: TextView = itemView.findViewById(R.id.sender_name)
        private val messageTimestamp: TextView = itemView.findViewById(R.id.message_timestamp)
        private val messageContent: TextView = itemView.findViewById(R.id.message_content)
        private val messageCard: MaterialCardView = itemView.findViewById(R.id.message_card)
        private val constraintLayout: ConstraintLayout = itemView as ConstraintLayout

        fun bind(message: ChatMessage) {
            // Set message content
            messageContent.text = message.content

            // Format and set timestamp
            message.timestamp?.let {
                messageTimestamp.text = dateFormat.format(it.toDate())
            }

            // Handle sent vs received messages
            if (message.senderId == currentUserId) {
                // This is a sent message - align right with blue background
                senderName.visibility = View.GONE
                messageCard.setCardBackgroundColor(itemView.context.getColor(R.color.sent_message_background))

                // Change constraints to align to the right
                val constraintSet = ConstraintSet()
                constraintSet.clone(constraintLayout)
                constraintSet.clear(R.id.message_card, ConstraintSet.START)
                constraintSet.connect(R.id.message_card, ConstraintSet.END, ConstraintSet.PARENT_ID, ConstraintSet.END)
                constraintSet.applyTo(constraintLayout)
            } else {
                // This is a received message - align left with gray background
                senderName.visibility = View.VISIBLE
                senderName.text = message.senderName
                messageCard.setCardBackgroundColor(itemView.context.getColor(R.color.received_message_background))

                // Change constraints to align to the left
                val constraintSet = ConstraintSet()
                constraintSet.clone(constraintLayout)
                constraintSet.clear(R.id.message_card, ConstraintSet.END)
                constraintSet.connect(R.id.message_card, ConstraintSet.START, ConstraintSet.PARENT_ID, ConstraintSet.START)
                constraintSet.applyTo(constraintLayout)
            }
        }
    }

    class MessageDiffCallback : DiffUtil.ItemCallback<ChatMessage>() {
        override fun areItemsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem.messageId == newItem.messageId
        }

        override fun areContentsTheSame(oldItem: ChatMessage, newItem: ChatMessage): Boolean {
            return oldItem == newItem
        }
    }
}