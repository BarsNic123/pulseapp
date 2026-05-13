package com.example.pulse

import android.content.Intent
import android.os.Bundle
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

class LoginActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_login)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.login)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(systemBars.left, systemBars.top, systemBars.right, systemBars.bottom)
            insets
        }

        val etEmail = findViewById<EditText>(R.id.etEmail)
        val etPassword = findViewById<EditText>(R.id.etPassword)
        val btnLogin = findViewById<MaterialButton>(R.id.btnLogin)
        val txtRegisterLink = findViewById<TextView>(R.id.txtRegisterLink)

        btnLogin.setOnClickListener {
            val email = etEmail.text.toString().trim()
            val password = etPassword.text.toString().trim()

            if (email.isEmpty() || password.isEmpty()) {
                Toast.makeText(this, "Please enter your credentials", Toast.LENGTH_SHORT).show()
                return@setOnClickListener
            }

            auth.signInWithEmailAndPassword(email, password)
                .addOnSuccessListener { result ->
                    val uid = result.user?.uid ?: return@addOnSuccessListener
                    loadUserRole(uid, result.user?.email)
                }
                .addOnFailureListener { e ->
                    Toast.makeText(this, "Login failed: ${e.message}", Toast.LENGTH_SHORT).show()
                }
        }

        txtRegisterLink.setOnClickListener {
            startActivity(Intent(this, RegisterActivity::class.java))
        }
    }

    private fun loadUserRole(uid: String, email: String?) {
        fun applyRole(role: String?) {
            when {
                role == PulseRoles.ROLE_ADMIN || PulseRoles.isAdminEmail(email) -> {
                    Toast.makeText(this, R.string.login_welcome_admin, Toast.LENGTH_SHORT).show()
                    goRootDashboard(AdminDashboardActivity::class.java)
                }
                role == PulseRoles.ROLE_RESPONDENT || role == PulseRoles.ROLE_RESPONDER -> {
                    Toast.makeText(this, "Welcome, responder", Toast.LENGTH_SHORT).show()
                    goRootDashboard(ResponderDashboardActivity::class.java)
                }
                role == PulseRoles.ROLE_PATIENT -> {
                    Toast.makeText(this, "Welcome", Toast.LENGTH_SHORT).show()
                    goRootDashboard(PatientDashboardActivity::class.java)
                }
                else -> {
                    auth.signOut()
                    Toast.makeText(
                        this,
                        "Profile has no valid role. Complete registration or contact support.",
                        Toast.LENGTH_LONG
                    ).show()
                }
            }
        }

        fun fetch(source: Source, onFail: () -> Unit) {
            db.collection("users").document(uid).get(source)
                .addOnSuccessListener { doc ->
                    if (!doc.exists()) {
                        if (PulseRoles.isAdminEmail(email)) {
                            Toast.makeText(this, R.string.login_welcome_admin, Toast.LENGTH_SHORT).show()
                            goRootDashboard(AdminDashboardActivity::class.java)
                            return@addOnSuccessListener
                        }
                        auth.signOut()
                        Toast.makeText(
                            this,
                            "No Firestore profile for this account. Register again or check Firebase.",
                            Toast.LENGTH_LONG
                        ).show()
                        return@addOnSuccessListener
                    }
                    val role = doc.getString("role")
                    val effectiveRole =
                        if (PulseRoles.isAdminEmail(email)) PulseRoles.ROLE_ADMIN else role
                    applyRole(effectiveRole)
                }
                .addOnFailureListener { onFail() }
        }

        fetch(Source.DEFAULT) {
            fetch(Source.CACHE) {
                auth.signOut()
                Toast.makeText(
                    this,
                    "Could not load your profile. Check network and Firestore rules, then try again.",
                    Toast.LENGTH_LONG
                ).show()
            }
        }
    }

    private fun goRootDashboard(dest: Class<*>) {
        startActivity(
            Intent(this, dest).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
        )
        finish()
    }
}
