package com.example.pulse

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
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

class RespondentRegActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_respondent_reg)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.respondent_reg)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val fullName = intent.getStringExtra("EXTRA_NAME") ?: ""
        val email = intent.getStringExtra("EXTRA_EMAIL") ?: ""
        val password = intent.getStringExtra("EXTRA_PASSWORD") ?: ""
        val authCompleted = intent.getBooleanExtra(RegisterActivity.EXTRA_AUTH_COMPLETED, false)

        val btnBack = findViewById<MaterialButton>(R.id.btnBack)
        val spinner = findViewById<Spinner>(R.id.spnExpertise)
        val etLicense = findViewById<EditText>(R.id.etLicense)
        val spnAffiliationCategory = findViewById<Spinner>(R.id.spnAffiliationCategory)
        val spnAffiliationInstitution = findViewById<Spinner>(R.id.spnAffiliationInstitution)
        val btnComplete = findViewById<Button>(R.id.btnVerifyComplete)

        val roles = arrayOf("Doctor", "Nurse", "MedTech", "Medical Student", "First Aider")
        spinner.adapter = ArrayAdapter(this, android.R.layout.simple_spinner_dropdown_item, roles)

        val categories = listOf(CebuInstitutions.TYPE_HOSPITAL, CebuInstitutions.TYPE_UNIVERSITY)
        spnAffiliationCategory.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            categories
        )

        fun reloadAffiliationInstitutions() {
            val type = spnAffiliationCategory.selectedItem?.toString() ?: CebuInstitutions.TYPE_HOSPITAL
            val items = if (type == CebuInstitutions.TYPE_UNIVERSITY) {
                CebuInstitutions.universitiesCebuCity
            } else {
                CebuInstitutions.hospitalsCebuCity
            }
            spnAffiliationInstitution.adapter = ArrayAdapter(
                this,
                android.R.layout.simple_spinner_dropdown_item,
                items
            )
        }

        spnAffiliationCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                reloadAffiliationInstitutions()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        reloadAffiliationInstitutions()

        btnBack.setOnClickListener { onBackPressedDispatcher.onBackPressed() }

        btnComplete.setOnClickListener {
            val license = etLicense.text.toString().trim()
            val selectedRole = spinner.selectedItem?.toString().orEmpty()
            val affiliationType = spnAffiliationCategory.selectedItem?.toString().orEmpty()
            val affiliatedInstitution = spnAffiliationInstitution.selectedItem?.toString().orEmpty()

            if (license.isEmpty() || affiliatedInstitution.isEmpty()) {
                Toast.makeText(this, R.string.respondent_reg_validation, Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            btnComplete.isEnabled = false

            val profileFields = hashMapOf<String, Any>(
                "fullName" to fullName,
                "email" to email,
                "role" to "Respondent",
                "expertise" to selectedRole,
                "license" to license,
                "affiliationType" to affiliationType,
                "affiliatedInstitution" to affiliatedInstitution,
                "organization" to affiliatedInstitution,
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
                        Toast.makeText(
                            this,
                            "Failed to save profile: ${e.message}",
                            Toast.LENGTH_LONG
                        ).show()
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
                    val uid = result.user?.uid ?: run {
                        btnComplete.isEnabled = true
                        return@addOnSuccessListener
                    }

                    val userMap = hashMapOf(
                        "uid" to uid,
                        "fullName" to fullName,
                        "email" to email,
                        "role" to "Respondent",
                        "expertise" to selectedRole,
                        "license" to license,
                        "affiliationType" to affiliationType,
                        "affiliatedInstitution" to affiliatedInstitution,
                        "organization" to affiliatedInstitution,
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
                            Toast.makeText(
                                this,
                                "Failed to save profile: ${e.message}",
                                Toast.LENGTH_LONG
                            ).show()
                        }
                }
                .addOnFailureListener { e ->
                    btnComplete.isEnabled = true
                    Toast.makeText(
                        this,
                        "Registration failed: ${e.message}",
                        Toast.LENGTH_LONG
                    ).show()
                }
        }
    }
}
