package com.example.pulse

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.RadioButton
import android.widget.RadioGroup
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class RegisterActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_register)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.register)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etFullName = findViewById<EditText>(R.id.etFullName)
        val etEmailRegister = findViewById<EditText>(R.id.etEmailRegister)
        val etPasswordRegister = findViewById<EditText>(R.id.etPasswordRegister)
        val rgRoleSelection = findViewById<RadioGroup>(R.id.rgRoleSelection)
        val btnRegisterSubmit = findViewById<MaterialButton>(R.id.btnRegisterSubmit)
        val btnBack = findViewById<MaterialButton>(R.id.btnBack)

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

            val role = when (radioButton.text.toString()) {
                "Patient" -> "Patient"
                "Responder" -> "Responder"
                else -> {
                    Toast.makeText(this, "Please select Patient or Responder", Toast.LENGTH_SHORT).show()
                    return@setOnClickListener
                }
            }

            btnRegisterSubmit.isEnabled = false

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val user = result.user
                    if (user == null) {
                        btnRegisterSubmit.isEnabled = true
                        return@addOnSuccessListener
                    }
                    val uid = user.uid
                    val minimal = hashMapOf(
                        "uid" to uid,
                        "fullName" to fullName,
                        "email" to email,
                        "role" to role,
                        "profileComplete" to false,
                        "createdAt" to FieldValue.serverTimestamp()
                    )

                    db.collection("users").document(uid).set(minimal)
                        .addOnSuccessListener {
                            btnRegisterSubmit.isEnabled = true
                            val next = when (role) {
                                "Patient" -> Intent(this, PatientRegActivity::class.java)
                                else -> Intent(this, RespondentRegActivity::class.java)
                            }
                            next.putExtra("EXTRA_NAME", fullName)
                            next.putExtra("EXTRA_EMAIL", email)
                            next.putExtra(EXTRA_AUTH_COMPLETED, true)
                            startActivity(next)
                            finish()
                        }
                        .addOnFailureListener { e ->
                            btnRegisterSubmit.isEnabled = true
                            user.delete().addOnCompleteListener { }
                            auth.signOut()
                            Toast.makeText(
                                this,
                                "Could not create your profile in Firestore: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
                .addOnFailureListener { e ->
                    btnRegisterSubmit.isEnabled = true
                    Toast.makeText(this, "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }

    companion object {
        const val EXTRA_AUTH_COMPLETED = "EXTRA_AUTH_COMPLETED"
    }
}
