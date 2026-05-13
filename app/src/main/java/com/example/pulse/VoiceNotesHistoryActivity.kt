package com.example.pulse

import android.os.Bundle
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class VoiceNotesHistoryActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private lateinit var adapter: VoiceNotesAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_voice_notes_history)

        val mode = intent.getStringExtra(EXTRA_MODE) ?: MODE_PATIENT

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.voiceHistoryRoot)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarVoiceHistory)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.title = if (mode == MODE_RESPONDER) {
            getString(R.string.voice_history_responder_title)
        } else {
            getString(R.string.voice_history_title)
        }

        val rv = findViewById<RecyclerView>(R.id.rvVoiceNotes)
        val progress = findViewById<ProgressBar>(R.id.progressVoiceHistory)

        adapter = VoiceNotesAdapter { msg ->
            Toast.makeText(this, getString(R.string.case_voice_error, msg), Toast.LENGTH_SHORT).show()
        }
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        progress.visibility = View.VISIBLE
        val uid = auth.currentUser?.uid
        if (uid == null) {
            progress.visibility = View.GONE
            Toast.makeText(this, R.string.responder_profile_need_login, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        val field = if (mode == MODE_RESPONDER) "responderUid" else "patientUid"
        db.collection("voice_notes")
            .whereEqualTo(field, uid)
            .limit(50)
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull { d ->
                    val url = d.getString("downloadUrl") ?: return@mapNotNull null
                    val ts = d.getTimestamp("createdAt")?.toDate()?.time ?: 0L
                    val title = if (mode == MODE_RESPONDER) {
                        getString(R.string.voice_history_from_patient, d.getString("patientName").orEmpty().ifBlank { "—" })
                    } else {
                        getString(R.string.voice_history_to_responder, d.getString("responderName").orEmpty().ifBlank { "—" })
                    }
                    VoiceNoteRow(d.id, title, url, ts)
                }.sortedByDescending { it.createdAtMillis }
                adapter.submit(list)
                progress.visibility = View.GONE
                if (list.isEmpty()) {
                    Toast.makeText(this, R.string.voice_history_empty, Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                progress.visibility = View.GONE
                Toast.makeText(this, getString(R.string.voice_history_error, e.message ?: ""), Toast.LENGTH_LONG).show()
            }
    }

    override fun onDestroy() {
        super.onDestroy()
        adapter.stop()
    }

    companion object {
        const val EXTRA_MODE = "EXTRA_MODE"
        const val MODE_PATIENT = "patient"
        const val MODE_RESPONDER = "responder"
    }
}
