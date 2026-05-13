package com.example.pulse

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PatientProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_patient_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        findViewById<MaterialButton>(R.id.btnBackProfile).setOnClickListener {
            finish()
        }

        findViewById<MaterialButton>(R.id.btnEditProfile).setOnClickListener {
            startActivity(Intent(this, PatientEditActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnLogout).setOnClickListener {
            AuthUi.logoutAndGoLogin(this)
        }

        loadProfile()
    }

    private fun loadProfile() {
        val uid = auth.currentUser?.uid
        val tvName = findViewById<TextView>(R.id.tvProfileName)
        val tvAge = findViewById<TextView>(R.id.tvProfileAge)
        val tvEmergency = findViewById<TextView>(R.id.tvProfileAddress)
        val tvBlood = findViewById<TextView>(R.id.tvProfileBlood)
        val tvAssigned = findViewById<TextView>(R.id.tvProfileAssigned)

        if (uid == null) {
            tvName.text = getString(R.string.dashboard_name_fallback)
            tvAge.text = "—"
            tvBlood.text = "—"
            tvEmergency.text = "—"
            tvAssigned.text = getString(R.string.profile_not_assigned)
            return
        }

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                tvName.text = doc.getString("fullName") ?: getString(R.string.dashboard_name_fallback)
                tvAge.text = doc.getString("age") ?: "—"
                tvBlood.text = doc.getString("bloodType") ?: "—"
                tvEmergency.text = doc.getString("emergencyContact") ?: "—"
                val assigned = doc.getString("assignedResponderName")?.trim().orEmpty()
                tvAssigned.text = if (assigned.isEmpty()) {
                    getString(R.string.profile_not_assigned)
                } else {
                    assigned
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, getString(R.string.profile_load_error, e.message ?: ""), Toast.LENGTH_LONG).show()
            }
    }
}
