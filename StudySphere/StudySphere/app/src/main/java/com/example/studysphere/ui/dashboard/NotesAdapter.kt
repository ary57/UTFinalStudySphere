// NotesAdapter.kt
package com.example.studysphere.ui.dashboard

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView
import com.example.studysphere.R
import com.example.studysphere.data.model.File
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale

class NotesAdapter(
    private var notes: List<File>,
    private val onNoteClicked: (File) -> Unit
) : RecyclerView.Adapter<NotesAdapter.NoteViewHolder>() {

    fun updateNotes(newNotes: List<File>) {
        notes = newNotes
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): NoteViewHolder {
        val view = LayoutInflater.from(parent.context)
            .inflate(R.layout.item_file, parent, false)
        return NoteViewHolder(view)
    }

    override fun onBindViewHolder(holder: NoteViewHolder, position: Int) {
        holder.bind(notes[position])
    }

    override fun getItemCount(): Int = notes.size

    inner class NoteViewHolder(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val titleTextView: TextView = itemView.findViewById(R.id.textViewFileName)
        private val dateTextView: TextView = itemView.findViewById(R.id.textViewFileDate)

        fun bind(file: File) {
            titleTextView.text = file.name

            // Format the date
            val sdf = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
            val formattedDate = sdf.format(Date(file.createdAt))
            dateTextView.text = formattedDate

            itemView.setOnClickListener {
                onNoteClicked(file)
            }
        }
    }
}