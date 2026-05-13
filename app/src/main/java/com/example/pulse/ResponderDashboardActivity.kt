package com.example.pulse

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class ResponderDashboardActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_responder_dashboard)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        findViewById<TextView>(R.id.tvRoleBadge).text = getString(R.string.role_badge_responder)

        findViewById<MaterialButton>(R.id.btnLogoutDashboard).setOnClickListener {
            AuthUi.logoutAndGoLogin(this)
        }

        val cvConfirmHelp = findViewById<MaterialCardView>(R.id.cvConfirmHelp)
        val cvViewDetails = findViewById<MaterialCardView>(R.id.cvViewDetails)
        val cvMyHelping = findViewById<MaterialCardView>(R.id.cvMyHelping)
        val cvHelpQueue = findViewById<MaterialCardView>(R.id.cvHelpQueue)
        val cvVoiceNotes = findViewById<MaterialCardView>(R.id.cvVoiceNotes)
        val navDashboard = findViewById<LinearLayout>(R.id.navDashboard)
        val navMyHelping = findViewById<LinearLayout>(R.id.navMyHelping)
        val navProfile = findViewById<LinearLayout>(R.id.navProfile)
        val scrollContent = findViewById<ScrollView>(R.id.scrollContent)

        fun openQueue(status: String) {
            startActivity(
                Intent(this, ResponderHelpQueueActivity::class.java)
                    .putExtra(ResponderHelpQueueActivity.EXTRA_STATUS_FILTER, status)
            )
        }

        cvConfirmHelp.setOnClickListener { openQueue(HelpRequestStatus.PENDING) }
        cvHelpQueue.setOnClickListener { openQueue(HelpRequestStatus.PENDING) }
        cvViewDetails.setOnClickListener { openQueue(HelpRequestStatus.ACCEPTED) }
        cvMyHelping.setOnClickListener { openQueue(HelpRequestStatus.DONE) }

        cvVoiceNotes.setOnClickListener {
            startActivity(
                Intent(this, VoiceNotesHistoryActivity::class.java)
                    .putExtra(VoiceNotesHistoryActivity.EXTRA_MODE, VoiceNotesHistoryActivity.MODE_RESPONDER)
            )
        }

        findViewById<TextView>(R.id.tvResponderVoiceHistoryLink).setOnClickListener {
            startActivity(
                Intent(this, VoiceNotesHistoryActivity::class.java)
                    .putExtra(VoiceNotesHistoryActivity.EXTRA_MODE, VoiceNotesHistoryActivity.MODE_RESPONDER)
            )
        }

        navDashboard.setOnClickListener { scrollContent.smoothScrollTo(0, 0) }
        navMyHelping.setOnClickListener { openQueue(HelpRequestStatus.DONE) }
        navProfile.setOnClickListener {
            startActivity(Intent(this, ResponderProfileActivity::class.java))
        }

        refreshResponderHeader()
    }

    override fun onResume() {
        super.onResume()
        refreshResponderHeader()
    }

    private fun firstName(full: String): String {
        val t = full.trim()
        if (t.isEmpty()) return getString(R.string.dashboard_name_fallback)
        return t.split(Regex("\\s+")).firstOrNull() ?: t
    }

    private fun refreshResponderHeader() {
        val tvHello = findViewById<TextView>(R.id.tvHelloUser)
        val uid = auth.currentUser?.uid
        if (uid == null) {
            tvHello.text = getString(R.string.hello_user_placeholder)
            return
        }
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("fullName") ?: getString(R.string.dashboard_name_fallback)
                tvHello.text = getString(R.string.hello_user_format, firstName(name))
            }
            .addOnFailureListener {
                tvHello.text = getString(R.string.hello_user_placeholder)
            }
    }
}
