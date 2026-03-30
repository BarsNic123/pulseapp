package com.example.pulse

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.ImageButton
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

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val btnBack = findViewById<ImageButton>(R.id.btnBack)
        val rgRoleSelection = findViewById<RadioGroup>(R.id.rgRoleSelection)
        val btnRegisterSubmit = findViewById<Button>(R.id.btnRegisterSubmit)

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnRegisterSubmit.setOnClickListener {
            // Check which role is selected
            val selectedId = rgRoleSelection.checkedRadioButtonId
            val radioButton = findViewById<RadioButton>(selectedId)

            when (radioButton.text.toString()) {
                "Patient" -> {
                    // Logic for Patient: Direct to Success or Patient Details
                    Toast.makeText(this, "Account Created! Welcome Patient.", Toast.LENGTH_SHORT).show()
                    // val intent = Intent(this, PatientDashboardActivity::class.java)
                    // startActivity(intent)
                }
                "Respondent" -> {
                    // Logic for Respondent: Switch to Details screen
                    val intent = Intent(this, RespondentRegActivity::class.java)
                    startActivity(intent)
                }
            }
        }
    }
}