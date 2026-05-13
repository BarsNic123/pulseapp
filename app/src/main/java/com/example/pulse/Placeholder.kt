package com.example.pulse

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth

class PlaceholderActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_placeholder)

        val tvTitle = findViewById<TextView>(R.id.tvTitle)
        val tvMessage = findViewById<TextView>(R.id.tvMessage)
        val btnBack = findViewById<MaterialButton>(R.id.btnBack)
        val btnPrimary = findViewById<MaterialButton>(R.id.btnPrimary)

        val title = intent.getStringExtra(EXTRA_TITLE) ?: "Details"
        val message = intent.getStringExtra(EXTRA_MESSAGE) ?: ""
        val primaryText = intent.getStringExtra(EXTRA_PRIMARY_TEXT) ?: "OK"
        val action = intent.getStringExtra(EXTRA_PRIMARY_ACTION)

        tvTitle.text = title
        tvMessage.text = message
        btnPrimary.text = primaryText

        val finishOnly = { finish() }

        val primaryHandler = when (action) {
            ACTION_LOGOUT -> {
                {
                    getSharedPreferences("PulsePrefs", MODE_PRIVATE).edit().clear().apply()
                    FirebaseAuth.getInstance().signOut()
                    startActivity(
                        Intent(this, LoginActivity::class.java).apply {
                            flags = Intent.FLAG_ACTIVITY_NEW_TASK or Intent.FLAG_ACTIVITY_CLEAR_TASK
                        }
                    )
                    finish()
                }
            }
            else -> finishOnly
        }

        btnBack.setOnClickListener { finishOnly() }
        btnPrimary.setOnClickListener { primaryHandler() }
    }

    companion object {
        const val EXTRA_TITLE = "EXTRA_TITLE"
        const val EXTRA_MESSAGE = "EXTRA_MESSAGE"
        const val EXTRA_PRIMARY_TEXT = "EXTRA_PRIMARY_TEXT"
        const val EXTRA_PRIMARY_ACTION = "EXTRA_PRIMARY_ACTION"

        const val ACTION_LOGOUT = "ACTION_LOGOUT"
    }
}
