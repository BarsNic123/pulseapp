package com.example.pulse

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RespondentRegActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_respondent_reg)

        // 1. Handle Window Insets for the Mosaic Background
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.respondent_reg)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // 2. Retrieve the data passed from the first Register screen
        val fullName = intent.getStringExtra("EXTRA_NAME")
        val email = intent.getStringExtra("EXTRA_EMAIL")
        val password = intent.getStringExtra("EXTRA_PASSWORD")

        // 3. Initialize UI Components
        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val spinner = findViewById<Spinner>(R.id.spnExpertise)
        val etLicense = findViewById<EditText>(R.id.etLicense)
        val etOrg = findViewById<EditText>(R.id.etOrganization)

        val btnComplete = findViewById<Button>(R.id.btnVerifyComplete)

        // 4. Setup the Expertise Spinner
        val roles = arrayOf("Doctor", "Nurse", "MedTech", "Medical Student", "First Aider")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)
        spinner.adapter = adapter

        // 5. Back Button
        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        // 6. Final Submit Logic
        btnComplete.setOnClickListener {
            val license = etLicense.text.toString().trim()
            val organization = etOrg.text.toString().trim()
            val selectedRole = spinner.selectedItem.toString()

            if (license.isEmpty() || organization.isEmpty()) {
                Toast.makeText(this, "Please fill in all professional details", Toast.LENGTH_SHORT).show()
            } else {
                // SUCCESS LOGIC:
                // You now have: fullName, email, password, selectedRole, license, and organization.
                // This is where you call your Spring Boot API to save the Respondent to MySQL.

                Toast.makeText(this, "Welcome, $selectedRole $fullName!", Toast.LENGTH_LONG).show()

                // Navigate to Respondent Dashboard or Login
                val intent = Intent(this, LoginActivity::class.java)
                startActivity(intent)
                finish() // Close registration screens
            }
        }
    }
}