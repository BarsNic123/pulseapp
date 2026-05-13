package com.example.pulse

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class PatientRegActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_patient_reg)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.patientReg)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val fullName = intent.getStringExtra("EXTRA_NAME") ?: ""
        val email = intent.getStringExtra("EXTRA_EMAIL") ?: ""
        val password = intent.getStringExtra("EXTRA_PASSWORD") ?: ""
        val authCompleted = intent.getBooleanExtra(RegisterActivity.EXTRA_AUTH_COMPLETED, false)

        val btnBack = findViewById<MaterialButton>(R.id.btnBack)
        val etAge = findViewById<EditText>(R.id.etAge)
        val spnBloodType = findViewById<Spinner>(R.id.spnBloodType)
        val etEmergencyContact = findViewById<EditText>(R.id.etEmergencyContact)
        val etMedicalNotes = findViewById<EditText>(R.id.etMedicalNotes)
        val rgRiskLevel = findViewById<RadioGroup>(R.id.rgRiskLevel)
        val btnComplete = findViewById<Button>(R.id.btnCompleteReg)

        ArrayAdapter.createFromResource(
            this,
            R.array.blood_types,
            android.R.layout.simple_spinner_item
        ).also { adapter ->
            adapter.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spnBloodType.adapter = adapter
        }

        btnBack.setOnClickListener { finish() }

        btnComplete.setOnClickListener {
            val age = etAge.text.toString().trim()
            val bloodType = spnBloodType.selectedItem?.toString()?.trim().orEmpty()
            val emergencyContact = etEmergencyContact.text.toString().trim()
            val medicalNotes = etMedicalNotes.text.toString().trim()

            if (age.isEmpty() || emergencyContact.isEmpty()) {
                Toast.makeText(this, "Please fill in all required fields", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }
            if (bloodType.isEmpty()) {
                Toast.makeText(this, "Please select your blood type", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val selectedRiskId = rgRiskLevel.checkedRadioButtonId
            if (selectedRiskId == -1) {
                Toast.makeText(this, "Please select your current medical status", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            val riskLevel = if (selectedRiskId == R.id.rbHighRisk) "High Risk" else "Low Risk"

            btnComplete.isEnabled = false

            val profileFields = hashMapOf<String, Any>(
                "fullName" to fullName,
                "email" to email,
                "role" to "Patient",
                "age" to age,
                "bloodType" to bloodType,
                "emergencyContact" to emergencyContact,
                "medicalNotes" to medicalNotes,
                "riskLevel" to riskLevel,
                "profileComplete" to true
            )

            if (authCompleted) {
                val user = auth.currentUser
                if (user == null || user.uid.isEmpty() || user.email?.equals(email, ignoreCase = true) != true) {
                    btnComplete.isEnabled = true
                    Toast.makeText(
                        this,
                        "Your sign-in session does not match this form. Please register again from the start.",
                        Toast.LENGTH_LONG
                    ).show()
                    return@setOnClickListener
                }
                val uid = user.uid
                profileFields["uid"] = uid
                db.collection("users").document(uid).set(profileFields, SetOptions.merge())
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            R.string.registration_complete_sign_in,
                            Toast.LENGTH_LONG
                        ).show()
                        AuthUi.logoutAndGoLogin(this)
                    }
                    .addOnFailureListener { e ->
                        btnComplete.isEnabled = true
                        Toast.makeText(this, "Failed to save profile: ${e.message}", Toast.LENGTH_LONG).show()
                    }
                return@setOnClickListener
            }

            if (password.isEmpty()) {
                btnComplete.isEnabled = true
                Toast.makeText(this, "Password missing. Go back and complete registration.", Toast.LENGTH_LONG).show()
                return@setOnClickListener
            }

            auth.createUserWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: return@addOnSuccessListener
                    val userMap = hashMapOf<String, Any>(
                        "uid" to uid,
                        "fullName" to fullName,
                        "email" to email,
                        "role" to "Patient",
                        "age" to age,
                        "bloodType" to bloodType,
                        "emergencyContact" to emergencyContact,
                        "medicalNotes" to medicalNotes,
                        "riskLevel" to riskLevel,
                        "profileComplete" to true
                    )

                    db.collection("users").document(uid).set(userMap)
                        .addOnSuccessListener {
                            Toast.makeText(
                                this,
                                R.string.registration_complete_sign_in,
                                Toast.LENGTH_LONG
                            ).show()
                            AuthUi.logoutAndGoLogin(this)
                        }
                        .addOnFailureListener { e ->
                            btnComplete.isEnabled = true
                            Toast.makeText(this, "Failed to save profile: ${e.message}", Toast.LENGTH_LONG).show()
                        }
                }
                .addOnFailureListener { e ->
                    btnComplete.isEnabled = true
                    Toast.makeText(this, "Registration failed: ${e.message}", Toast.LENGTH_LONG).show()
                }
        }
    }
}
