package com.example.pulse

import android.os.Bundle
import android.widget.Button
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PatientDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_patient_dashboard)

        // Edge-to-edge padding fix (Updated to use android.R.id.content)
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        // 1. Find the new buttons using the IDs we just added to the XML
        val cvRecordVoice = findViewById<CardView>(R.id.cvRecordVoice)
        val cvRequestRide = findViewById<CardView>(R.id.cvRequestRide)
        val btnSos = findViewById<Button>(R.id.btnSos)

        // 2. Set Click Listeners for each action
        cvRecordVoice?.setOnClickListener {
            Toast.makeText(this, "Listening for voice command...", Toast.LENGTH_SHORT).show()
        }

        cvRequestRide?.setOnClickListener {
            // This is where your Dispatch/Ride-hailing Integration will go
            Toast.makeText(this, "Searching for nearest available responder...", Toast.LENGTH_LONG).show()
        }

        btnSos?.setOnClickListener {
            Toast.makeText(this, "EMERGENCY SOS TRIGGERED!", Toast.LENGTH_LONG).show()
        }
    }
}