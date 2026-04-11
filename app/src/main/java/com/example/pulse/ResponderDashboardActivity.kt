package com.example.pulse

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.cardview.widget.CardView
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat

class ResponderDashboardActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_responder_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        val cvConfirmHelp = findViewById<CardView>(R.id.cvConfirmHelp)
        val cvProfileCard = findViewById<CardView>(R.id.cvProfileCard)
        val navProfile = findViewById<LinearLayout>(R.id.navProfile)

        cvConfirmHelp.setOnClickListener {
            val intent = Intent(this, CaseTrackerActivity::class.java).apply {
                putExtra("EXTRA_PATIENT_NAME", "Lola Rosa")
                putExtra("EXTRA_RISK_LEVEL", "High Risk")
                putExtra("EXTRA_LOCATION", "Dr. Santos Clinic")
                putExtra("EXTRA_NOTES", "Needs assistance getting to Dr. Santos Clinic.")
            }
            startActivity(intent)
        }

        val openEditProfile = {
            val intent = Intent(this, ResponderEditActivity::class.java)
            startActivity(intent)
        }

        cvProfileCard?.setOnClickListener { openEditProfile() }
        navProfile?.setOnClickListener { openEditProfile() }
    }
}