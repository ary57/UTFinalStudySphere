package com.example.studysphere

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Button
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import com.google.firebase.auth.FirebaseAuth

class LoginFragment : Fragment() {
    private lateinit var auth: FirebaseAuth

    private lateinit var emailInput: TextInputEditText
    private lateinit var passwordInput: TextInputEditText
    private lateinit var emailInputLayout: TextInputLayout
    private lateinit var passwordInputLayout: TextInputLayout
    private lateinit var loginButton: Button
    private lateinit var registerButton: Button

    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        val view = inflater.inflate(R.layout.fragment_login, container, false)

        // Initialize Firebase Authentication
        auth = FirebaseAuth.getInstance()

        // Find views
        emailInput = view.findViewById(R.id.et_email)
        passwordInput = view.findViewById(R.id.et_password)
        emailInputLayout = view.findViewById(R.id.til_email)
        passwordInputLayout = view.findViewById(R.id.til_password)
        loginButton = view.findViewById(R.id.btn_login)
        registerButton = view.findViewById(R.id.btn_register)

        // Setup login button
        loginButton.setOnClickListener {
            performLogin()
        }

        // Setup register button
        registerButton.setOnClickListener {
            // TODO: Navigate to registration fragment
            Toast.makeText(context, "Registration coming soon", Toast.LENGTH_SHORT).show()
        }

        return view
    }

    private fun performLogin() {
        // Reset error states
        emailInputLayout.error = null
        passwordInputLayout.error = null

        val email = emailInput.text.toString().trim()
        val password = passwordInput.text.toString()

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

        // Show loading state
        loginButton.isEnabled = false

        // firebase auth
        auth.signInWithEmailAndPassword(email, password)
            .addOnCompleteListener { task ->
                // Re-enable login button
                loginButton.isEnabled = true

                if (task.isSuccessful) {
                    // Navigate to Dashboard
                    Toast.makeText(context, "Login Successful", Toast.LENGTH_SHORT).show()
                    // [Done] TODO: Navigate to dashboard
                    findNavController().navigate(R.id.action_login_to_dashboard)
                } else {
                    // Handle login failures
                    val errorMessage = when (task.exception) {
                        is com.google.firebase.auth.FirebaseAuthInvalidCredentialsException ->
                            "Invalid email or password"
                        is com.google.firebase.auth.FirebaseAuthInvalidUserException ->
                            "No account found with this email"
                        else ->
                            "Login failed: ${task.exception?.message}"
                    }

                    Toast.makeText(context, errorMessage, Toast.LENGTH_LONG).show()
                }
            }
    }
}