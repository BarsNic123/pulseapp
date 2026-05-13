package com.example.pulse

import android.content.Intent
import android.os.Bundle
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class AdminDashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.admin)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.adminRoot)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, bars.bottom)
            insets
        }

        findViewById<TextView>(R.id.tvRoleBadge).text = getString(R.string.role_badge_admin)

        findViewById<MaterialButton>(R.id.btnLogoutDashboard).setOnClickListener {
            AuthUi.logoutAndGoLogin(this)
        }

        val scrollContent = findViewById<ScrollView>(R.id.scrollContent)
        findViewById<android.widget.LinearLayout>(R.id.navDashboard).setOnClickListener {
            scrollContent.smoothScrollTo(0, 0)
        }
        findViewById<android.widget.LinearLayout>(R.id.navPatients).setOnClickListener {
            startActivity(Intent(this, PatientListActivity::class.java))
        }
        findViewById<android.widget.LinearLayout>(R.id.navHelpOps).setOnClickListener {
            startActivity(Intent(this, AdminHelpOverviewActivity::class.java))
        }
        findViewById<android.widget.LinearLayout>(R.id.navFunds).setOnClickListener {
            startActivity(Intent(this, TransparencyDashboardActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cvAdminPatients).setOnClickListener {
            startActivity(Intent(this, PatientListActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.cvAdminResponders).setOnClickListener {
            startActivity(
                Intent(this, ResponderListActivity::class.java)
                    .putExtra(ResponderListActivity.EXTRA_ASSIGN_MODE, false)
            )
        }
        findViewById<MaterialCardView>(R.id.cvAdminHelp).setOnClickListener {
            startActivity(Intent(this, AdminHelpOverviewActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.cvAdminFunds).setOnClickListener {
            startActivity(Intent(this, TransparencyDashboardActivity::class.java))
        }

        bindHeader()
    }

    override fun onResume() {
        super.onResume()
        FinanceFirestore.seedDefaultsIfEmpty(db, auth) { }
        loadLiveCounts()
    }

    private fun bindHeader() {
        val user = auth.currentUser
        val tvHello = findViewById<TextView>(R.id.tvHelloUser)
        val display = user?.displayName?.takeIf { it.isNotBlank() }
            ?: user?.email?.substringBefore("@")?.takeIf { it.isNotBlank() }
        tvHello.text = if (display != null) {
            getString(R.string.hello_admin_named, display)
        } else {
            getString(R.string.hello_admin_placeholder)
        }
    }

    private fun loadLiveCounts() {
        val strip = findViewById<TextView>(R.id.tvAdminOverviewStrip)
        val helpCountView = findViewById<TextView>(R.id.tvAdminHelpCount)
        strip.setText(R.string.admin_overview_strip_loading)
        helpCountView.text = "—"

        db.collection("users").get()
            .addOnSuccessListener { userSnap ->
                var patients = 0
                var responders = 0
                for (doc in userSnap.documents) {
                    when (doc.getString("role")) {
                        PulseRoles.ROLE_PATIENT -> patients++
                        PulseRoles.ROLE_RESPONDER, PulseRoles.ROLE_RESPONDENT -> responders++
                    }
                }
                db.collection("helpRequests").get()
                    .addOnSuccessListener { helpSnap ->
                        val helpTotal = helpSnap.size()
                        helpCountView.text = helpTotal.toString()
                        strip.text = getString(
                            R.string.admin_overview_strip_format,
                            patients,
                            responders,
                            helpTotal
                        )
                    }
                    .addOnFailureListener { e ->
                        strip.text = getString(R.string.admin_overview_strip_error)
                        Toast.makeText(
                            this,
                            getString(R.string.help_list_load_error, e.message ?: ""),
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .addOnFailureListener { e ->
                strip.text = getString(R.string.admin_overview_strip_error)
                Toast.makeText(
                    this,
                    getString(R.string.patient_list_cloud_unavailable) + "\n" + (e.message ?: ""),
                    Toast.LENGTH_LONG
                ).show()
            }
    }
}
