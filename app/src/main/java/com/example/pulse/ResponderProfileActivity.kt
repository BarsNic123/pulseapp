package com.example.pulse

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ResponderProfileActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_responder_profile)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        findViewById<MaterialButton>(R.id.btnBack).setOnClickListener { finish() }

        findViewById<MaterialButton>(R.id.btnEditResponderProfile).setOnClickListener {
            startActivity(Intent(this, ResponderEditActivity::class.java))
        }

        findViewById<MaterialButton>(R.id.btnLogoutResponder).setOnClickListener {
            AuthUi.logoutAndGoLogin(this)
        }

        loadProfile()
    }

    override fun onResume() {
        super.onResume()
        loadProfile()
    }

    private fun loadProfile() {
        val uid = auth.currentUser?.uid
        val tvName = findViewById<TextView>(R.id.tvResponderName)
        val tvEmail = findViewById<TextView>(R.id.tvResponderEmail)
        val tvExpertise = findViewById<TextView>(R.id.tvResponderExpertise)
        val tvAffiliation = findViewById<TextView>(R.id.tvResponderAffiliation)
        val tvLicense = findViewById<TextView>(R.id.tvResponderLicense)

        if (uid == null) {
            Toast.makeText(this, R.string.responder_profile_need_login, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                tvName.text = doc.getString("fullName") ?: "—"
                tvEmail.text = doc.getString("email") ?: "—"
                tvExpertise.text = doc.getString("expertise") ?: "—"
                val affType = doc.getString("affiliationType").orEmpty()
                val affPlace = doc.getString("affiliatedInstitution").orEmpty()
                val org = doc.getString("organization").orEmpty()
                tvAffiliation.text = when {
                    affType.isNotBlank() && affPlace.isNotBlank() -> "$affType · $affPlace"
                    affPlace.isNotBlank() -> affPlace
                    org.isNotBlank() -> org
                    else -> "—"
                }
                tvLicense.text = doc.getString("license") ?: "—"
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, getString(R.string.profile_load_error, e.message ?: ""), Toast.LENGTH_LONG).show()
            }
    }
}
