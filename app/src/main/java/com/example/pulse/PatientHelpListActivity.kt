package com.example.pulse

import android.content.Intent
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

class PatientHelpListActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: HelpRequestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_patient_help_list)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.patientHelpListRoot)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        findViewById<MaterialToolbar>(R.id.toolbarHelpList).setNavigationOnClickListener { finish() }

        val rv = findViewById<RecyclerView>(R.id.rvHelpRequests)
        val progress = findViewById<ProgressBar>(R.id.progressHelpList)

        adapter = HelpRequestAdapter(showPatient = true) { item ->
            startActivity(
                Intent(this, CaseTrackerActivity::class.java)
                    .putExtra(CaseTrackerActivity.EXTRA_HELP_REQUEST_ID, item.id)
                    .putExtra(CaseTrackerActivity.EXTRA_VIEWER, CaseTrackerActivity.VIEWER_PATIENT)
            )
        }
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        val progress = findViewById<ProgressBar>(R.id.progressHelpList)
        progress.visibility = View.VISIBLE
        loadAndMarkSeen(progress)
    }

    private fun loadAndMarkSeen(progress: ProgressBar) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            progress.visibility = View.GONE
            Toast.makeText(this, R.string.responder_assign_need_login, Toast.LENGTH_LONG).show()
            finish()
            return
        }
        db.collection("helpRequests")
            .whereEqualTo("patientUid", uid)
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull { HelpRequest.from(it) }
                    .sortedByDescending { it.createdAtMillis }
                adapter.submitList(list)
                markDoneSeen(uid, list)
                progress.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                progress.visibility = View.GONE
                Toast.makeText(this, getString(R.string.help_list_load_error, e.message ?: ""), Toast.LENGTH_LONG).show()
            }
    }

    private fun markDoneSeen(patientUid: String, list: List<HelpRequest>) {
        val unseen = list.filter { it.status == HelpRequestStatus.DONE && !it.patientSeenCompletion }
        if (unseen.isEmpty()) return
        val batch = db.batch()
        unseen.forEach { req ->
            val ref = db.collection("helpRequests").document(req.id)
            batch.update(ref, mapOf("patientSeenCompletion" to true))
        }
        batch.commit().addOnFailureListener { /* non-fatal */ }
    }
}
