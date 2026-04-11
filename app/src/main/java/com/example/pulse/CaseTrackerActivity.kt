package com.example.pulse

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.ImageView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class CaseTrackerActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_case_tracker)

        // Handle edge-to-edge insets
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.headerLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(16, systemBars.top + 16, 16, 16)
            insets
        }

        val patientName = intent.getStringExtra("EXTRA_PATIENT_NAME") ?: "Unknown Patient"
        val riskLevel = intent.getStringExtra("EXTRA_RISK_LEVEL") ?: "Unknown"
        val location = intent.getStringExtra("EXTRA_LOCATION") ?: "Unknown"
        val notes = intent.getStringExtra("EXTRA_NOTES") ?: "No notes provided."

        val tvPatientName = findViewById<TextView>(R.id.tvPatientName)
        val tvRiskLevel = findViewById<TextView>(R.id.tvRiskLevel)
        val tvLocation = findViewById<TextView>(R.id.tvLocation)
        val tvMedicalNotes = findViewById<TextView>(R.id.tvMedicalNotes)
        val tvCaseStatus = findViewById<TextView>(R.id.tvCaseStatus)

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val btnAccept = findViewById<Button>(R.id.btnAccept)
        val btnInProgress = findViewById<Button>(R.id.btnInProgress)
        val btnDone = findViewById<Button>(R.id.btnDone)

        tvPatientName.text = patientName
        tvRiskLevel.text = riskLevel
        tvLocation.text = location
        tvMedicalNotes.text = notes


        btnAccept.visibility = View.GONE


        btnBack.setOnClickListener {
            finish()
        }

        btnInProgress.setOnClickListener {
            tvCaseStatus.text = "STATUS: IN-PROGRESS"
            tvCaseStatus.setTextColor(Color.parseColor("#4A90E2")) // Blue text
            Toast.makeText(this, "Tracking started.", Toast.LENGTH_SHORT).show()


            btnInProgress.visibility = View.GONE
            btnDone.visibility = View.VISIBLE
        }

        btnDone.setOnClickListener {
            Toast.makeText(this, "Case completed. Great job!", Toast.LENGTH_LONG).show()

            finish()
        }
    }
}