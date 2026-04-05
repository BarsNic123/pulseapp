package com.example.pulse

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView // Make sure this is imported!
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class RegisterActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        // Window insets to keep the content above the navigation bar
        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        // Initialize Input Fields
        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etEmailRegister = findViewById<EditText>(R.id.etEmailRegister)
        val etPasswordRegister = findViewById<EditText>(R.id.etPasswordRegister)

        // Initialize Buttons and Groups
        val rgRoleSelection = findViewById<RadioGroup>(R.id.rgRoleSelection)
        val btnRegisterSubmit = findViewById<Button>(R.id.btnRegisterSubmit)

        // THE FIX: This is now an ImageView to match the XML
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        btnBack.setOnClickListener {
            // Replaced deprecated onBackPressed with the modern approach or finish()
            finish()
        }

        btnRegisterSubmit.setOnClickListener {
            // Check which role is selected
            val selectedId = rgRoleSelection.checkedRadioButtonId

            if (selectedId == -1) {
                Toast.makeText(this, "Please select an account type", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val radioButton = findViewById<RadioButton>(selectedId)

            // Extract the user data
            val fullName = etFullName.text.toString().trim()
            val email = etEmailRegister.text.toString().trim()
            val password = etPasswordRegister.text.toString().trim()

            // Basic Validation
            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            when (radioButton.text.toString()) {
                "Patient" -> {
                    // Logic for Patient
                    Toast.makeText(this, "Account Created! Welcome Patient.", Toast.LENGTH_SHORT).show()

                    val intent = Intent(this, LoginActivity::class.java)
                    intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                    startActivity(intent)
                }
                "Respondent" -> {
                    // Logic for Respondent: Pass data to next screen
                    val intent = Intent(this, RespondentRegActivity::class.java).apply {
                        putExtra("EXTRA_NAME", fullName)
                        putExtra("EXTRA_EMAIL", email)
                        putExtra("EXTRA_PASSWORD", password)
                    }
                    startActivity(intent)
                }
            }
        }
    }
}