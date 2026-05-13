package com.example.pulse

import android.media.MediaRecorder
import android.net.Uri
import android.os.Bundle
import android.os.SystemClock
import android.view.View
import android.widget.Chronometer
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.storage.FirebaseStorage
import java.io.File
import java.io.IOException
import java.util.UUID

class VoiceRecorderActivity : AppCompatActivity() {

    private var mediaRecorder: MediaRecorder? = null
    private var isRecording = false
    private var audioPath: String? = null

    private lateinit var tvStatus: TextView
    private lateinit var chronometer: Chronometer
    private lateinit var btnBack: MaterialButton
    private lateinit var fabRecord: FloatingActionButton
    private lateinit var btnSend: MaterialButton

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var storage: FirebaseStorage

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_voice_recorder)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        storage = FirebaseStorage.getInstance()

        tvStatus = findViewById(R.id.tvRecorderStatus)
        chronometer = findViewById(R.id.chronometerRecord)
        btnBack = findViewById(R.id.btnBack)
        fabRecord = findViewById(R.id.fabRecord)
        btnSend = findViewById(R.id.btnSendRecording)

        chronometer.isCountDown = false
        chronometer.visibility = View.GONE

        btnBack.setOnClickListener {
            if (isRecording) {
                stopRecording()
            }
            finish()
        }

        fabRecord.setOnClickListener {
            if (isRecording) {
                stopRecording()
            } else {
                startRecording()
            }
        }

        btnSend.setOnClickListener {
            uploadRecording()
        }
    }

    private fun startRecording() {
        val file = File(cacheDir, "voice_${System.currentTimeMillis()}.m4a")
        audioPath = file.absolutePath

        mediaRecorder = MediaRecorder().apply {
            setAudioSource(MediaRecorder.AudioSource.MIC)
            setOutputFormat(MediaRecorder.OutputFormat.MPEG_4)
            setAudioEncoder(MediaRecorder.AudioEncoder.AAC)
            setOutputFile(audioPath)

            try {
                prepare()
                start()
                isRecording = true
                tvStatus.text = getString(R.string.voice_recording_status)
                chronometer.stop()
                chronometer.base = SystemClock.elapsedRealtime()
                chronometer.start()
                chronometer.visibility = View.VISIBLE
                fabRecord.setImageResource(android.R.drawable.ic_media_pause)
                btnSend.visibility = View.GONE
            } catch (e: IOException) {
                Toast.makeText(this@VoiceRecorderActivity, R.string.voice_recording_failed, Toast.LENGTH_SHORT).show()
            }
        }
    }

    private fun stopRecording() {
        try {
            mediaRecorder?.apply {
                stop()
                release()
            }
        } catch (e: Exception) {
            e.printStackTrace()
        }
        mediaRecorder = null
        isRecording = false

        chronometer.stop()
        tvStatus.text = getString(R.string.voice_recording_saved)
        fabRecord.setImageResource(android.R.drawable.ic_btn_speak_now)
        btnSend.visibility = View.VISIBLE
    }

    override fun onDestroy() {
        chronometer.stop()
        super.onDestroy()
    }

    private fun uploadRecording() {
        val path = audioPath
        val uid = auth.currentUser?.uid
        if (path.isNullOrBlank() || uid.isNullOrBlank()) {
            Toast.makeText(this, R.string.voice_upload_need_file, Toast.LENGTH_LONG).show()
            return
        }
        val file = File(path)
        if (!file.exists()) {
            Toast.makeText(this, R.string.voice_upload_need_file, Toast.LENGTH_LONG).show()
            return
        }

        btnSend.isEnabled = false
        tvStatus.text = getString(R.string.voice_uploading)

        db.collection("users").document(uid).get()
            .addOnSuccessListener { userDoc ->
                val patientName = userDoc.getString("fullName").orEmpty().ifBlank { "Patient" }
                val responderUid = userDoc.getString("assignedResponderUid").orEmpty()
                val responderName = userDoc.getString("assignedResponderName").orEmpty()
                if (responderUid.isEmpty()) {
                    btnSend.isEnabled = true
                    tvStatus.text = getString(R.string.voice_recording_saved)
                    Toast.makeText(this, R.string.voice_upload_need_responder, Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }

                val storageRef = storage.reference.child("voice_notes/$uid/${UUID.randomUUID()}.m4a")
                storageRef.putFile(Uri.fromFile(file))
                    .addOnSuccessListener {
                        storageRef.downloadUrl
                            .addOnSuccessListener { uri ->
                                val downloadUrl = uri.toString()
                                val note = hashMapOf(
                                    "patientUid" to uid,
                                    "patientName" to patientName,
                                    "responderUid" to responderUid,
                                    "responderName" to responderName,
                                    "downloadUrl" to downloadUrl,
                                    "storagePath" to storageRef.path,
                                    "createdAt" to FieldValue.serverTimestamp()
                                )
                                db.collection("voice_notes").add(note)
                                    .addOnSuccessListener {
                                        PulsePrefs.saveLastVoiceUrl(this@VoiceRecorderActivity, downloadUrl)
                                        Toast.makeText(this@VoiceRecorderActivity, R.string.voice_upload_success, Toast.LENGTH_LONG).show()
                                        finish()
                                    }
                                    .addOnFailureListener { e ->
                                        btnSend.isEnabled = true
                                        tvStatus.text = getString(R.string.voice_recording_saved)
                                        Toast.makeText(this@VoiceRecorderActivity, getString(R.string.voice_upload_error, e.message ?: ""), Toast.LENGTH_LONG).show()
                                    }
                            }
                            .addOnFailureListener { e ->
                                btnSend.isEnabled = true
                                tvStatus.text = getString(R.string.voice_recording_saved)
                                Toast.makeText(this@VoiceRecorderActivity, getString(R.string.voice_upload_error, e.message ?: ""), Toast.LENGTH_LONG).show()
                            }
                    }
                    .addOnFailureListener { e ->
                        btnSend.isEnabled = true
                        tvStatus.text = getString(R.string.voice_recording_saved)
                        Toast.makeText(this@VoiceRecorderActivity, getString(R.string.voice_upload_error, e.message ?: ""), Toast.LENGTH_LONG).show()
                    }
            }
            .addOnFailureListener { e ->
                btnSend.isEnabled = true
                tvStatus.text = getString(R.string.voice_recording_saved)
                Toast.makeText(this, getString(R.string.voice_upload_error, e.message ?: ""), Toast.LENGTH_LONG).show()
            }
    }
}
