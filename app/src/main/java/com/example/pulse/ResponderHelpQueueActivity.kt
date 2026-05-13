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

class ResponderHelpQueueActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: HelpRequestAdapter
    private lateinit var statusFilter: String

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_responder_help_queue)

        statusFilter = intent.getStringExtra(EXTRA_STATUS_FILTER) ?: HelpRequestStatus.PENDING

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.responderQueueRoot)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarQueue)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.title = when (statusFilter) {
            HelpRequestStatus.PENDING -> getString(R.string.responder_queue_confirm_title)
            HelpRequestStatus.ACCEPTED -> getString(R.string.responder_queue_active_title)
            HelpRequestStatus.DONE -> getString(R.string.responder_queue_done_title)
            else -> getString(R.string.responder_queue_confirm_title)
        }

        val rv = findViewById<RecyclerView>(R.id.rvQueue)
        val progress = findViewById<ProgressBar>(R.id.progressQueue)

        adapter = HelpRequestAdapter(showPatient = false) { item ->
            startActivity(
                Intent(this, CaseTrackerActivity::class.java)
                    .putExtra(CaseTrackerActivity.EXTRA_HELP_REQUEST_ID, item.id)
                    .putExtra(CaseTrackerActivity.EXTRA_VIEWER, CaseTrackerActivity.VIEWER_RESPONDER)
            )
        }
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter
    }

    override fun onResume() {
        super.onResume()
        val progress = findViewById<ProgressBar>(R.id.progressQueue)
        progress.visibility = View.VISIBLE
        loadQueue(statusFilter, progress)
    }

    private fun loadQueue(statusFilter: String, progress: ProgressBar) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            progress.visibility = View.GONE
            Toast.makeText(this, R.string.responder_profile_need_login, Toast.LENGTH_LONG).show()
            finish()
            return
        }
        db.collection("helpRequests")
            .whereEqualTo("responderUid", uid)
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull { HelpRequest.from(it) }
                    .filter { it.status == statusFilter }
                    .sortedByDescending { it.createdAtMillis }
                adapter.submitList(list)
                progress.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                progress.visibility = View.GONE
                Toast.makeText(this, getString(R.string.help_list_load_error, e.message ?: ""), Toast.LENGTH_LONG).show()
            }
    }

    companion object {
        const val EXTRA_STATUS_FILTER = "EXTRA_STATUS_FILTER"
    }
}
