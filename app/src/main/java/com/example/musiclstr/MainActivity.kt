package com.example.musiclstr

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_splash) // Use the new splash screen layout

        Handler().postDelayed({
            val intent = if (userIsLoggedIn()) {
                Intent(this, HomeScreen::class.java) // Go to HomeScreen if logged in
            } else {
                Intent(this, SignInActivity::class.java) // Go to SignInActivity if not logged in
            }
            startActivity(intent)
            finish() // Close the splash screen
        }, 3000) // 3 seconds delay
    }

    private fun userIsLoggedIn(): Boolean {
        // Implement logic to check if the user is logged in, e.g., check a token in SharedPreferences
        return false // Replace with actual login check
    }
}
