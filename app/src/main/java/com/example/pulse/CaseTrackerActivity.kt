package com.example.pulse

import android.graphics.Color
import android.media.MediaPlayer
import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class CaseTrackerActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var auth: FirebaseAuth
    private var listener: ListenerRegistration? = null
    private var mediaPlayer: MediaPlayer? = null

    private var helpRequestId: String? = null
    private var viewer: String = VIEWER_RESPONDER
    private var current: HelpRequest? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_case_tracker)

        db = FirebaseFirestore.getInstance()
        auth = FirebaseAuth.getInstance()

        helpRequestId = intent.getStringExtra(EXTRA_HELP_REQUEST_ID)
        viewer = intent.getStringExtra(EXTRA_VIEWER) ?: VIEWER_RESPONDER

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.headerLayout)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(16, systemBars.top + 16, 16, 16)
            insets
        }

        findViewById<MaterialButton>(R.id.btnBack).setOnClickListener { finish() }

        val title = findViewById<android.widget.TextView>(R.id.tvCaseToolbarTitle)
        title.text = if (viewer == VIEWER_PATIENT) {
            getString(R.string.case_tracker_patient_title)
        } else {
            getString(R.string.case_tracker_title)
        }

        findViewById<MaterialButton>(R.id.btnAccept).setOnClickListener { acceptCase() }
        findViewById<MaterialButton>(R.id.btnPlayVoice).setOnClickListener { togglePlayVoice() }
        findViewById<MaterialButton>(R.id.btnEditPatient).setOnClickListener {
            val pUid = current?.patientUid ?: return@setOnClickListener
            startActivity(
                android.content.Intent(this, PatientEditActivity::class.java)
                    .putExtra(PatientEditActivity.EXTRA_TARGET_UID, pUid)
                    .putExtra(PatientEditActivity.EXTRA_EDITED_BY_RESPONDER, true)
            )
        }

        val id = helpRequestId
        if (id.isNullOrBlank()) {
            Toast.makeText(this, R.string.case_missing_id, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        attachListener(id)
    }

    private fun attachListener(id: String) {
        listener = db.collection("helpRequests").document(id).addSnapshotListener { snap, e ->
            if (e != null || snap == null || !snap.exists()) {
                Toast.makeText(this, R.string.case_load_error, Toast.LENGTH_LONG).show()
                return@addSnapshotListener
            }
            val model = HelpRequest.from(snap) ?: return@addSnapshotListener
            current = model
            bindUi(model)
        }
    }

    private fun bindUi(model: HelpRequest) {
        val tvStatus = findViewById<android.widget.TextView>(R.id.tvCaseStatus)
        tvStatus.text = getString(R.string.case_status_format, model.statusLabel())

        when (model.status) {
            HelpRequestStatus.PENDING -> {
                tvStatus.setTextColor(Color.parseColor("#F57F17"))
            }
            HelpRequestStatus.ACCEPTED -> {
                tvStatus.setTextColor(Color.parseColor("#1A73E8"))
            }
            HelpRequestStatus.DONE -> {
                tvStatus.setTextColor(Color.parseColor("#1E8E3E"))
            }
            else -> tvStatus.setTextColor(Color.parseColor("#616161"))
        }

        findViewById<android.widget.TextView>(R.id.tvPatientName).text = model.patientName.ifBlank { getString(R.string.dashboard_name_fallback) }
        findViewById<android.widget.TextView>(R.id.tvPatientAge).text = model.patientAge.ifBlank { "—" }
        findViewById<android.widget.TextView>(R.id.tvRiskLevel).text = model.patientRiskLevel.ifBlank { "—" }
        findViewById<android.widget.TextView>(R.id.tvLocation).text = model.locationSummary.ifBlank { getString(R.string.help_row_no_location) }
        findViewById<android.widget.TextView>(R.id.tvBloodType).text = model.patientBloodType.ifBlank { "—" }
        findViewById<android.widget.TextView>(R.id.tvEmergency).text = model.patientEmergency.ifBlank { "—" }
        findViewById<android.widget.TextView>(R.id.tvMedicalNotes).text = model.patientMedicalNotes.ifBlank { getString(R.string.case_no_notes) }

        val voiceUrl = model.voiceUrl
        val btnPlay = findViewById<MaterialButton>(R.id.btnPlayVoice)
        if (!voiceUrl.isNullOrBlank()) {
            btnPlay.visibility = View.VISIBLE
        } else {
            btnPlay.visibility = View.GONE
            stopVoice()
        }

        val btnAccept = findViewById<MaterialButton>(R.id.btnAccept)
        val switchDone = findViewById<SwitchMaterial>(R.id.switchDoneResponding)
        val btnEdit = findViewById<MaterialButton>(R.id.btnEditPatient)

        if (viewer == VIEWER_PATIENT) {
            btnAccept.visibility = View.GONE
            switchDone.visibility = View.GONE
            btnEdit.visibility = View.GONE
            switchDone.isChecked = false
            return
        }

        val uid = auth.currentUser?.uid
        val isAssignedResponder = uid != null && uid == model.responderUid
        if (!isAssignedResponder) {
            btnAccept.visibility = View.GONE
            switchDone.visibility = View.GONE
            btnEdit.visibility = View.GONE
            return
        }

        when (model.status) {
            HelpRequestStatus.PENDING -> {
                btnAccept.visibility = View.VISIBLE
                switchDone.visibility = View.GONE
                btnEdit.visibility = View.GONE
                switchDone.setOnCheckedChangeListener(null)
                switchDone.isChecked = false
            }
            HelpRequestStatus.ACCEPTED -> {
                btnAccept.visibility = View.GONE
                switchDone.visibility = View.VISIBLE
                btnEdit.visibility = View.VISIBLE
                switchDone.setOnCheckedChangeListener(null)
                switchDone.isChecked = false
                switchDone.isEnabled = true
                switchDone.setOnCheckedChangeListener { _, isChecked ->
                    if (isChecked) markDone()
                }
            }
            HelpRequestStatus.DONE -> {
                btnAccept.visibility = View.GONE
                switchDone.visibility = View.VISIBLE
                btnEdit.visibility = View.VISIBLE
                switchDone.setOnCheckedChangeListener(null)
                switchDone.isChecked = true
                switchDone.isEnabled = false
            }
            else -> {
                btnAccept.visibility = View.GONE
                switchDone.visibility = View.GONE
                btnEdit.visibility = View.GONE
            }
        }
    }

    private fun acceptCase() {
        val id = helpRequestId ?: return
        db.collection("helpRequests").document(id).update(
            mapOf(
                "status" to HelpRequestStatus.ACCEPTED,
                "acceptedAt" to FieldValue.serverTimestamp()
            )
        ).addOnSuccessListener {
            Toast.makeText(this, R.string.case_accepted_toast, Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Toast.makeText(this, getString(R.string.case_update_error, e.message ?: ""), Toast.LENGTH_LONG).show()
        }
    }

    private fun markDone() {
        val id = helpRequestId ?: return
        db.collection("helpRequests").document(id).update(
            mapOf(
                "status" to HelpRequestStatus.DONE,
                "completedAt" to FieldValue.serverTimestamp(),
                "patientSeenCompletion" to false
            )
        ).addOnSuccessListener {
            Toast.makeText(this, R.string.case_done_toast, Toast.LENGTH_SHORT).show()
        }.addOnFailureListener { e ->
            Toast.makeText(this, getString(R.string.case_update_error, e.message ?: ""), Toast.LENGTH_LONG).show()
            findViewById<SwitchMaterial>(R.id.switchDoneResponding).apply {
                setOnCheckedChangeListener(null)
                isChecked = false
            }
            current?.let { bindUi(it) }
        }
    }

    private fun togglePlayVoice() {
        val url = current?.voiceUrl ?: return
        if (mediaPlayer?.isPlaying == true) {
            stopVoice()
            findViewById<MaterialButton>(R.id.btnPlayVoice).text = getString(R.string.case_play_voice)
            return
        }
        stopVoice()
        try {
            mediaPlayer = MediaPlayer().apply {
                setDataSource(url)
                setOnCompletionListener {
                    findViewById<MaterialButton>(R.id.btnPlayVoice).text = getString(R.string.case_play_voice)
                    stopVoice()
                }
                prepare()
                start()
            }
            findViewById<MaterialButton>(R.id.btnPlayVoice).text = getString(R.string.case_stop_voice)
        } catch (ex: Exception) {
            Toast.makeText(this, getString(R.string.case_voice_error, ex.message ?: ""), Toast.LENGTH_LONG).show()
        }
    }

    private fun stopVoice() {
        try {
            mediaPlayer?.release()
        } catch (_: Exception) {
        }
        mediaPlayer = null
    }

    override fun onDestroy() {
        super.onDestroy()
        listener?.remove()
        stopVoice()
    }

    companion object {
        const val EXTRA_HELP_REQUEST_ID = "EXTRA_HELP_REQUEST_ID"
        const val EXTRA_VIEWER = "EXTRA_VIEWER"
        const val VIEWER_PATIENT = "patient"
        const val VIEWER_RESPONDER = "responder"
    }
}
