package com.example.studysphere.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView
import com.example.studysphere.data.model.StudySession
import com.example.studysphere.databinding.ItemStudySessionBinding
import com.google.firebase.auth.FirebaseAuth
import java.text.SimpleDateFormat
import java.util.Locale

class StudySessionAdapter(
    private val onRsvpClick: (String, String) -> Unit
) : ListAdapter<StudySession, StudySessionAdapter.StudySessionViewHolder>(DIFF_CALLBACK) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): StudySessionViewHolder {
        val binding = ItemStudySessionBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return StudySessionViewHolder(binding)
    }

    override fun onBindViewHolder(holder: StudySessionViewHolder, position: Int) {
        holder.bind(getItem(position))
    }

    inner class StudySessionViewHolder(
        private val binding: ItemStudySessionBinding
    ) : RecyclerView.ViewHolder(binding.root) {

        fun bind(session: StudySession) {
            binding.apply {
                textViewSessionTitle.text = session.title

                val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
                val date = dateFormat.format(session.date.toDate())
                textViewSessionDateTime.text = "$date • ${session.time}"

                textViewSessionLocation.text = session.location

                val attendees = session.rsvps.count { it.value == StudySession.RSVP_YES }
                textViewSessionAttendees.text = "$attendees people attending"

                // Get current user's RSVP status
                val currentUserId = FirebaseAuth.getInstance().currentUser?.uid
                val currentUserRsvp = if (currentUserId != null) {
                    session.rsvps[currentUserId]
                } else null

                textViewYourRsvp.text = when (currentUserRsvp) {
                    StudySession.RSVP_YES -> "Your RSVP: Going"
                    StudySession.RSVP_MAYBE -> "Your RSVP: Maybe"
                    StudySession.RSVP_NO -> "Your RSVP: Not going"
                    else -> "Your RSVP: Not responded"
                }

                // Set button states
                buttonRsvpYes.isSelected = currentUserRsvp == StudySession.RSVP_YES
                buttonRsvpMaybe.isSelected = currentUserRsvp == StudySession.RSVP_MAYBE
                buttonRsvpNo.isSelected = currentUserRsvp == StudySession.RSVP_NO

                // Set click listeners
                buttonRsvpYes.setOnClickListener {
                    onRsvpClick(session.sessionId, StudySession.RSVP_YES)
                }

                buttonRsvpMaybe.setOnClickListener {
                    onRsvpClick(session.sessionId, StudySession.RSVP_MAYBE)
                }

                buttonRsvpNo.setOnClickListener {
                    onRsvpClick(session.sessionId, StudySession.RSVP_NO)
                }
            }
        }
    }

    companion object {
        private val DIFF_CALLBACK = object : DiffUtil.ItemCallback<StudySession>() {
            override fun areItemsTheSame(oldItem: StudySession, newItem: StudySession): Boolean {
                return oldItem.sessionId == newItem.sessionId
            }

            override fun areContentsTheSame(oldItem: StudySession, newItem: StudySession): Boolean {
                return oldItem == newItem
            }
        }
    }
}