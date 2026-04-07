package com.example.pulse

import android.content.Intent
import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.ImageButton
import android.widget.ImageView
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import java.util.concurrent.Executors

class RespondentRegActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_respondent_reg)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.respondent_reg)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        // Retrieve data from RegisterActivity
        val fullName = intent.getStringExtra("EXTRA_NAME") ?: ""
        val email = intent.getStringExtra("EXTRA_EMAIL") ?: ""
        val password = intent.getStringExtra("EXTRA_PASSWORD") ?: ""

        val btnBack = findViewById<ImageView>(R.id.btnBack)
        val spinner = findViewById<Spinner>(R.id.spnExpertise)
        val etLicense = findViewById<EditText>(R.id.etLicense)
        val etOrg = findViewById<EditText>(R.id.etOrganization)
        val btnComplete = findViewById<Button>(R.id.btnVerifyComplete)

        val roles = arrayOf("Doctor", "Nurse", "MedTech", "Medical Student", "First Aider")
        val adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)
        spinner.adapter = adapter

        btnBack.setOnClickListener {
            onBackPressedDispatcher.onBackPressed()
        }

        btnComplete.setOnClickListener {
            val license = etLicense.text.toString().trim()
            val organization = etOrg.text.toString().trim()
            val selectedRole = spinner.selectedItem.toString()

            if (license.isEmpty() || organization.isEmpty()) {
                Toast.makeText(this, "Please fill in all professional details", Toast.LENGTH_SHORT).show()
            } else {
                // Background Thread for SQL Operation
                val executor = Executors.newSingleThreadExecutor()
                executor.execute {
                    val connectionClass = ConnectionClass()
                    val conn = connectionClass.CONN()

                    if (conn != null) {
                        try {
                            // SQL INSERT Query including all fields
                            val sql = """
                                INSERT INTO Users (email, password, fullName, role, license, organization) 
                                VALUES ('$email', '$password', '$fullName', '$selectedRole', '$license', '$organization')
                            """.trimIndent()

                            val statement = conn.createStatement()
                            statement.executeUpdate(sql)

                            runOnUiThread {
                                Toast.makeText(this, "Registration Successful, $fullName!", Toast.LENGTH_LONG).show()
                                val intent = Intent(this, MainActivity::class.java)
                                intent.flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                startActivity(intent)
                            }
                        } catch (e: Exception) {
                            e.printStackTrace()
                            runOnUiThread {
                                Toast.makeText(this, "DB Error: ${e.message}", Toast.LENGTH_LONG).show()
                            }
                        }
                    } else {
                        runOnUiThread {
                            Toast.makeText(this, "Server Connection Failed", Toast.LENGTH_SHORT).show()
                        }
                    }
                }
            }
        }
    }
}