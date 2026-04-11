package com.example.pulse

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class LoginActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<Button>(R.id.btnLogin)
        val txtRegisterLink = findViewById<TextView>(R.id.txtRegisterLink)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter your credentials", Toast.LENGTH_SHORT).show()
            } else {
                // Hardcoded Admin Access for testing
                if (email == "admin@pulse.com" && password == "admin123") {
                    Toast.makeText(this, "Admin Login Successful", Toast.LENGTH_SHORT).show()
                    startActivity(Intent(this, AdminDashboardActivity::class.java))
                    finish()
                    return@setOnClickListener
                }

                // Normal User Role Retrieval
                val sharedPreferences = getSharedPreferences("PulsePrefs", MODE_PRIVATE)
                val savedEmail = sharedPreferences.getString("USER_EMAIL", "")
                val savedRole = sharedPreferences.getString("USER_ROLE", "Patient")

                // Simple check for simulation: if not matching saved email,
                // we check if the word "responder" is in the email string
                val finalRole = if (email == savedEmail) {
                    savedRole
                } else if (email.contains("responder", ignoreCase = true)) {
                    "Respondent"
                } else {
                    "Patient"
                }

                when (finalRole) {
                    "Respondent" -> {
                        Toast.makeText(this, "Welcome, Responder", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, ResponderDashboardActivity::class.java))
                    }
                    "Patient" -> {
                        Toast.makeText(this, "Welcome, Patient", Toast.LENGTH_SHORT).show()
                        startActivity(Intent(this, PatientDashboardActivity::class.java))
                    }
                    else -> {
                        Toast.makeText(this, "Role not recognized", Toast.LENGTH_SHORT).show()
                    }
                }
                finish()
            }
        }

        txtRegisterLink.setOnClickListener {
            val intent = Intent(this, RegisterActivity::class.java)
            startActivity(intent)
        }
    }
}