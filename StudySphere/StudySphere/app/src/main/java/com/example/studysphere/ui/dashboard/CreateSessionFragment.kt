package com.example.studysphere.ui.dashboard

import android.app.DatePickerDialog
import android.app.TimePickerDialog
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.lifecycle.ViewModelProvider
import androidx.navigation.fragment.findNavController
import androidx.navigation.fragment.navArgs
import com.example.studysphere.data.model.StudySession
import com.example.studysphere.databinding.FragmentCreateSessionBinding
import com.google.firebase.Timestamp
import java.text.SimpleDateFormat
import java.util.Calendar
import java.util.Locale

class CreateSessionFragment : Fragment() {

    private var _binding: FragmentCreateSessionBinding? = null
    private val binding get() = _binding!!

    private lateinit var viewModel: MeetsViewModel
    private val args: CreateSessionFragmentArgs by navArgs()

    private val calendar = Calendar.getInstance()

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View {
        _binding = FragmentCreateSessionBinding.inflate(inflater, container, false)
        return binding.root
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel = ViewModelProvider(this)[MeetsViewModel::class.java]

        setupDatePicker()
        setupTimePicker()
        setupButtons()
    }

    private fun setupDatePicker() {
        binding.editTextDate.setOnClickListener {
            val year = calendar.get(Calendar.YEAR)
            val month = calendar.get(Calendar.MONTH)
            val day = calendar.get(Calendar.DAY_OF_MONTH)

            DatePickerDialog(
                requireContext(),
                { _, selectedYear, selectedMonth, selectedDay ->
                    calendar.set(Calendar.YEAR, selectedYear)
                    calendar.set(Calendar.MONTH, selectedMonth)
                    calendar.set(Calendar.DAY_OF_MONTH, selectedDay)
                    updateDateField()
                },
                year,
                month,
                day
            ).show()
        }
    }

    private fun setupTimePicker() {
        binding.editTextTime.setOnClickListener {
            val hour = calendar.get(Calendar.HOUR_OF_DAY)
            val minute = calendar.get(Calendar.MINUTE)

            TimePickerDialog(
                requireContext(),
                { _, selectedHour, selectedMinute ->
                    calendar.set(Calendar.HOUR_OF_DAY, selectedHour)
                    calendar.set(Calendar.MINUTE, selectedMinute)
                    updateTimeField()
                },
                hour,
                minute,
                false
            ).show()
        }
    }

    private fun updateDateField() {
        val dateFormat = SimpleDateFormat("MMM dd, yyyy", Locale.getDefault())
        binding.editTextDate.setText(dateFormat.format(calendar.time))
    }

    private fun updateTimeField() {
        val timeFormat = SimpleDateFormat("h:mm a", Locale.getDefault())
        binding.editTextTime.setText(timeFormat.format(calendar.time))
    }

    private fun setupButtons() {
        binding.buttonCancel.setOnClickListener {
            findNavController().navigateUp()
        }

        binding.buttonSave.setOnClickListener {
            if (validateInputs()) {
                val title = binding.editTextTitle.text.toString().trim()
                val location = binding.editTextLocation.text.toString().trim()
                val description = binding.editTextDescription.text.toString().trim()
                val time = binding.editTextTime.text.toString().trim()

                val timestamp = Timestamp(calendar.time)

                val studySession = StudySession(
                    courseId = args.courseId,
                    title = title,
                    date = timestamp,
                    time = time,
                    location = location,
                    description = description
                )

                viewModel.createStudySession(
                    studySession,
                    onSuccess = {
                        Toast.makeText(
                            context,
                            "Study session created successfully",
                            Toast.LENGTH_SHORT
                        ).show()
                        findNavController().navigateUp()
                    },
                    onFailure = { error ->
                        Toast.makeText(context, error, Toast.LENGTH_SHORT).show()
                    }
                )
            }
        }
    }

    private fun validateInputs(): Boolean {
        var isValid = true

        if (binding.editTextTitle.text.toString().trim().isEmpty()) {
            binding.textInputLayoutTitle.error = "Title cannot be empty"
            isValid = false
        } else {
            binding.textInputLayoutTitle.error = null
        }

        if (binding.editTextDate.text.toString().isEmpty()) {
            binding.textInputLayoutDate.error = "Please select a date"
            isValid = false
        } else {
            binding.textInputLayoutDate.error = null
        }

        if (binding.editTextTime.text.toString().isEmpty()) {
            binding.textInputLayoutTime.error = "Please select a time"
            isValid = false
        } else {
            binding.textInputLayoutTime.error = null
        }

        if (binding.editTextLocation.text.toString().trim().isEmpty()) {
            binding.textInputLayoutLocation.error = "Location cannot be empty"
            isValid = false
        } else {
            binding.textInputLayoutLocation.error = null
        }

        return isValid
    }

    override fun onDestroyView() {
        super.onDestroyView()
        _binding = null
    }
}