package com.example.pulse

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class PatientRegActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_patient_reg)

        // Handle Window Insets for proper padding
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.patientReg)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Retrieve data passed from the main Register screen
        val fullName = intent.getStringExtra("EXTRA_NAME") ?: ""
        val email = intent.getStringExtra("EXTRA_EMAIL") ?: ""
        val password = intent.getStringExtra("EXTRA_PASSWORD") ?: ""

        // Initialize UI Components
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val etAge = findViewById<EditText>(R.id.etAge)
        val etBloodType = findViewById<EditText>(R.id.etBloodType)
        val etEmergencyContact = findViewById<EditText>(R.id.etEmergencyContact)
        val etMedicalNotes = findViewById<EditText>(R.id.etMedicalNotes)
        val rgRiskLevel = findViewById<RadioGroup>(R.id.rgRiskLevel)
        val btnComplete = findViewById<Button>(R.id.btnCompleteReg)

        btnBack.setOnClickListener {
            finish()
        }

        btnComplete.setOnClickListener {
            val age = etAge.text.toString().trim()
            val emergencyContact = etEmergencyContact.text.toString().trim()

            if (age.isEmpty() || emergencyContact.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedRiskId = rgRiskLevel.checkedRadioButtonId
            if (selectedRiskId == -1) {
                Toast.makeText(this, "Please select your current medical status", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            // Determine if they are high or low risk for the database
            val riskLevel = if (selectedRiskId == R.id.rbHighRisk) "High Risk" else "Low Risk"

            // SUCCESS LOGIC: Send the data to your backend
            Toast.makeText(this, "Welcome, $fullName! Profile created as Patient ($riskLevel).", Toast.LENGTH_LONG).show()

            // Navigate to Main/Login
            val intent = Intent(this, LoginActivity::class.java)
            intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            startActivity(intent)
        }
    }
}