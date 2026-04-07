package com.example.pulse

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity

class MainActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        // 2-Second Delay before switching screens
        Handler(Looper.getMainLooper()).postDelayed({

            // Check session (Placeholder for now)
            val isLoggedIn = false

            if (isLoggedIn) {
                // If you had a saved user type, you'd navigate to the dashboard
                startActivity(Intent(this, PatientDashboardActivity::class.java))
            } else {
                // New users or logged out users go to Login
                startActivity(Intent(this, LoginActivity::class.java))
            }

            finish() // Important: Closes the launcher so the user can't "back" into it
        }, 2000)
    }
}