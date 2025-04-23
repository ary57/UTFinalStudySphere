package com.example.studysphere.ui.auth

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.example.studysphere.R
import com.example.studysphere.ValidationUtils
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class RegisterFragment : Fragment() {
    private lateinit var auth: FirebaseAuth
    private lateinit var firestore: FirebaseFirestore

    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var confirmPasswordInput: TextInputEditText
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var confirmPasswordInputLayout: TextInputLayout
    private lateinit var registerButton: Button
    private lateinit var loginLinkButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_register, container, false)

        // Initialize Firebase Services
        auth = FirebaseAuth.getInstance()
        firestore = FirebaseFirestore.getInstance()

        // Find views
        emailInput = view.findViewById(R.id.et_register_email)
        passwordInput = view.findViewById(R.id.et_register_password)
        confirmPasswordInput = view.findViewById(R.id.et_register_confirm_password)
        emailInputLayout = view.findViewById(R.id.til_register_email)
        passwordInputLayout = view.findViewById(R.id.til_register_password)
        confirmPasswordInputLayout = view.findViewById(R.id.til_register_confirm_password)
        registerButton = view.findViewById(R.id.btn_register_submit)
        loginLinkButton = view.findViewById(R.id.btn_goto_login)

        // Setup register button
        registerButton.setOnClickListener {
            performRegistration()
        }

        // Setup login link button
        loginLinkButton.setOnClickListener {
            findNavController().navigate(R.id.action_registerFragment_to_loginFragment)
        }

        return view
    }

    private fun performRegistration() {
        // Reset error states
        emailInputLayout.error = null
        passwordInputLayout.error = null
        confirmPasswordInputLayout.error = null

        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()
        val confirmPassword = confirmPasswordInput.text.toString()

        // Validate email
        if (email.isEmpty()) {
            emailInputLayout.error = "Email cannot be empty"
            return
        }

        if (!ValidationUtils.isValidEmail(email)) {
            emailInputLayout.error = "Invalid email format"
            return
        }

        // Validate password
        if (password.isEmpty()) {
            passwordInputLayout.error = "Password cannot be empty"
            return
        }

        if (!ValidationUtils.isValidPassword(password)) {
            passwordInputLayout.error = "Password must be at least 6 characters"
            return
        }

        // Validate confirm password
        if (confirmPassword.isEmpty()) {
            confirmPasswordInputLayout.error = "Please confirm your password"
            return
        }

        if (password != confirmPassword) {
            confirmPasswordInputLayout.error = "Passwords do not match"
            return
        }

        // Show loading state
        registerButton.isEnabled = false

        // Create user with Firebase Auth
        auth.createUserWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                if (task.isSuccessful) {
                    // Create user document in Firestore
                    createUserInFirestore(email)
                } else {
                    // Re-enable register button
                    registerButton.isEnabled = true

                    // Handle registration failures
                    val errorMessage = when (task.exception) {
                        is com.google.firebase.auth.FirebaseAuthUserCollisionException ->
                            "An account with this email already exists"
                        is com.google.firebase.auth.FirebaseAuthWeakPasswordException ->
                            "Password is too weak"
                        is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ->
                            "Invalid email format"
                        else ->
                            "Registration failed: ${task.exception?.message}"
                    }

                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }

    private fun createUserInFirestore(email: String) {
        val userId = auth.currentUser?.uid
        if (userId != null) {
            val userMap = hashMapOf(
                "userId" to userId,
                "email" to email,
                "enrolledCourses" to listOf<String>()
            )

            firestore.collection("users").document(userId)
                .set(userMap)
                .addOnSuccessListener {
                    registerButton.isEnabled = true
                    Toast.makeText(context, "Registration successful", Toast.LENGTH_SHORT).show()
                    findNavController().navigate(R.id.action_registerFragment_to_dashboardFragment)
                }
                .addOnFailureListener { e ->
                    registerButton.isEnabled = true
                    Toast.makeText(
                        context,
                        "Failed to save user data: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        } else {
            registerButton.isEnabled = true
            Toast.makeText(context, "Registration failed: User ID not found", Toast.LENGTH_LONG).show()
        }
    }
}