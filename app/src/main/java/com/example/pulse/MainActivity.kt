package com.example.pulse

import android.content.Intent
import android.os.Bundle
import android.os.Handler
import android.os.Looper
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.Source

class MainActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_main)

        Handler(Looper.getMainLooper()).postDelayed({
            routeAfterSplash()
        }, 1800)
    }

    private fun routeAfterSplash() {
        val auth = FirebaseAuth.getInstance()
        val user = auth.currentUser
        if (user == null) {
            goLogin()
            return
        }

        val db = FirebaseFirestore.getInstance()
        val uid = user.uid
        val email = user.email

        fun openDashboard(role: String?) {
            val effectiveRole =
                if (PulseRoles.isAdminEmail(email)) PulseRoles.ROLE_ADMIN else role
            val dest = when (effectiveRole) {
                PulseRoles.ROLE_ADMIN -> AdminDashboardActivity::class.java
                PulseRoles.ROLE_RESPONDENT, PulseRoles.ROLE_RESPONDER -> ResponderDashboardActivity::class.java
                PulseRoles.ROLE_PATIENT -> PatientDashboardActivity::class.java
                else -> null
            }
            if (dest == null) {
                auth.signOut()
                goLogin()
                return
            }
            startActivity(
                Intent(this, dest).addFlags(
                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                )
            )
            finish()
        }

        fun loadProfile(source: Source, onMissing: () -> Unit) {
            db.collection("users").document(uid).get(source)
                .addOnSuccessListener { doc ->
                    if (!doc.exists()) {
                        if (PulseRoles.isAdminEmail(email)) {
                            startActivity(
                                Intent(this, AdminDashboardActivity::class.java).addFlags(
                                    Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                                )
                            )
                            finish()
                            return@addOnSuccessListener
                        }
                        onMissing()
                        return@addOnSuccessListener
                    }
                    openDashboard(doc.getString("role"))
                }
                .addOnFailureListener { onMissing() }
        }

        loadProfile(Source.DEFAULT) {
            loadProfile(Source.CACHE) {
                auth.signOut()
                goLogin()
            }
        }
    }

    private fun goLogin() {
        startActivity(
            Intent(this, LoginActivity::class.java).addFlags(
                Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
            )
        )
        finish()
    }
}
