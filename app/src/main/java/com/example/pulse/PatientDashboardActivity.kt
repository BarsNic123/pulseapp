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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        val cvRecordVoice = findViewById<CardView>(R.id.cvRecordVoice)
        val cvRequestRide = findViewById<CardView>(R.id.cvRequestRide)
        val btnSos = findViewById<Button>(R.id.btnSos)

        cvRecordVoice?.setOnClickListener {
            Toast.makeText(this, "Listening for voice command...", Toast.LENGTH_SHORT).show()
        }
        cvRequestRide?.setOnClickListener {
            Toast.makeText(this, "Searching for nearest available responder...", Toast.LENGTH_LONG).show()
        }
        btnSos?.setOnClickListener {
            Toast.makeText(this, "EMERGENCY SOS TRIGGERED!", Toast.LENGTH_LONG).show()
        }
    }
}