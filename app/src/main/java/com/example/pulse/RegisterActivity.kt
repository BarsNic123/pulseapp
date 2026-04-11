package com.example.pulse

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.ImageView
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

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etEmailRegister = findViewById<EditText>(R.id.etEmailRegister)
        val etPasswordRegister = findViewById<EditText>(R.id.etPasswordRegister)
        val rgRoleSelection = findViewById<RadioGroup>(R.id.rgRoleSelection)
        val btnRegisterSubmit = findViewById<Button>(R.id.btnRegisterSubmit)
        val btnBack = findViewById<ImageView>(R.id.btnBack)

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnRegisterSubmit.setOnClickListener {
            val selectedId = rgRoleSelection.checkedRadioButtonId

            if (selectedId == -1) {
                Toast.makeText(this, "Please select an account type", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val radioButton = findViewById<RadioButton>(selectedId)
            val fullName = etFullName.text.toString().trim()
            val email = etEmailRegister.text.toString().trim()
            val password = etPasswordRegister.text.toString().trim()

            if (fullName.isEmpty() || email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please fill out all fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            when (radioButton.text.toString()) {
                "Patient" -> {
                    // Navigate to Patient Registration Step 2
                    val intent = Intent(this, PatientRegActivity::class.java).apply {
                        putExtra("EXTRA_NAME", fullName)
                        putExtra("EXTRA_EMAIL", email)
                        putExtra("EXTRA_PASSWORD", password)
                    }
                    startActivity(intent)
                }
                "Respondent" -> {
                    // Navigate to Respondent Registration Step 2
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