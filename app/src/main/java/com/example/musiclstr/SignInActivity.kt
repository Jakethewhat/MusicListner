package com.example.musiclstr

import android.content.Intent
import android.os.Bundle
import android.text.InputType
import android.util.Log
import android.view.View
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import retrofit2.Call
import retrofit2.Callback
import retrofit2.Response

class SignInActivity : AppCompatActivity() {

    private lateinit var usernameEditText: EditText
    private lateinit var passwordEditText: EditText
    private lateinit var signInButton: Button
    private lateinit var signUpRedirectText: TextView
    private lateinit var togglePasswordButton: ImageView // Moved this up for better access

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        Log.d("SignInActivity", "onCreate: Initializing SignInActivity")
        setContentView(R.layout.activity_login) // Ensure the correct layout file

        // Initialize views
        usernameEditText = findViewById(R.id.editTextText) // Update with correct ID
        passwordEditText = findViewById(R.id.editTextText2) // Update with correct ID
        signInButton = findViewById(R.id.signInButton)
        signUpRedirectText = findViewById(R.id.signUpRedirectText)
        togglePasswordButton = findViewById(R.id.togglePasswordButton)

        // Set the initial state of the password field to hidden
        passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
        togglePasswordButton.setImageResource(R.drawable.eye_close) // Default to eye closed

        // Initialize toggle button for password visibility
        var isPasswordVisible = false

        // Set click listener for toggle button
        togglePasswordButton.setOnClickListener {
            if (isPasswordVisible) {
                // Hide password
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_PASSWORD
                togglePasswordButton.setImageResource(R.drawable.eye_close) // Use appropriate drawable
            } else {
                // Show password
                passwordEditText.inputType = InputType.TYPE_CLASS_TEXT or InputType.TYPE_TEXT_VARIATION_VISIBLE_PASSWORD
                togglePasswordButton.setImageResource(R.drawable.eye_open) // Use appropriate drawable
            }
            isPasswordVisible = !isPasswordVisible
            passwordEditText.setSelection(passwordEditText.text.length) // Keep cursor at the end
        }

        // Set click listener for the sign-in button
        signInButton.setOnClickListener {
            val username = usernameEditText.text.toString()
            val password = passwordEditText.text.toString()
            loginUser(username, password) // Call loginUser function
        }

        // Set click listener for sign-up redirect text
        signUpRedirectText.setOnClickListener {
            // Intent to redirect to sign-up activity
            val intent = Intent(this, SignUpActivity::class.java)
            startActivity(intent)
        }
    }

    private fun loginUser(username: String, password: String) {
        // Show progress bar
        findViewById<ProgressBar>(R.id.progressBar).visibility = View.VISIBLE

        val loginRequest = LoginRequest(username, password)

        RetrofitClient.instance.login(loginRequest).enqueue(object : Callback<ApiResponse> {
            override fun onResponse(call: Call<ApiResponse>, response: Response<ApiResponse>) {
                // Hide progress bar
                findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE

                if (response.isSuccessful) {
                    val responseBody = response.body()
                    responseBody?.let {
                        Log.d("SignInActivity", "Login successful, navigating to HomeScreen")
                        Toast.makeText(this@SignInActivity, it.message, Toast.LENGTH_SHORT).show()
                        // Redirect to HomeScreen
                        val intent = Intent(this@SignInActivity, HomeScreen::class.java)
                        startActivity(intent)
                        finish()
                    }
                } else {
                    Toast.makeText(this@SignInActivity, "Login failed: ${response.errorBody()?.string()}", Toast.LENGTH_LONG).show()
                }
            }

            override fun onFailure(call: Call<ApiResponse>, t: Throwable) {
                // Hide progress bar
                findViewById<ProgressBar>(R.id.progressBar).visibility = View.GONE
                Toast.makeText(this@SignInActivity, "Error: ${t.message}", Toast.LENGTH_SHORT).show()
            }
        })
    }
}
