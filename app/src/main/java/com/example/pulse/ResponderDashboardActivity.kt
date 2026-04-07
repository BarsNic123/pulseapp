package com.example.pulse

import android.os.Bundle
import android.widget.Button
import android.widget.LinearLayout
import android.widget.Switch
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ResponderDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_responder_dashboard)

        // Edge-to-edge padding fix
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.responderDashboard)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        val switchStatus = findViewById<Switch>(R.id.switchStatus)
        val btnAcceptCase = findViewById<Button>(R.id.btnAcceptCase)
        val cardRequest = findViewById<LinearLayout>(R.id.cardRequest)

        // Active/Inactive Toggle Logic
        switchStatus.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                switchStatus.text = "Status: Active"
                Toast.makeText(this, "You are now visible to patients.", Toast.LENGTH_SHORT).show()
            } else {
                switchStatus.text = "Status: Inactive"
                Toast.makeText(this, "You are offline.", Toast.LENGTH_SHORT).show()
            }
        }

        // Accept Case Logic
        btnAcceptCase.setOnClickListener {
            Toast.makeText(this, "Case Accepted! Tracking started.", Toast.LENGTH_LONG).show()
            btnAcceptCase.text = "Mark as Done"
            btnAcceptCase.setBackgroundColor(android.graphics.Color.parseColor("#4CAF50")) // Turn green

            // If it's already accepted, mark it as done and hide the card
            btnAcceptCase.setOnClickListener {
                cardRequest.visibility = android.view.View.GONE
                Toast.makeText(this, "Case completed. Thank you for your service!", Toast.LENGTH_SHORT).show()
            }
        }
    }
}