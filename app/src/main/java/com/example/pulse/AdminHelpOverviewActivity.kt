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

/** Admin-only list of all HELP requests (read-only case view on tap). */
class AdminHelpOverviewActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: HelpRequestAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_responder_help_queue)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.responderQueueRoot)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        findViewById<MaterialToolbar>(R.id.toolbarQueue).apply {
            setNavigationOnClickListener { finish() }
            title = getString(R.string.admin_help_overview_title)
        }

        val rv = findViewById<RecyclerView>(R.id.rvQueue)
        val progress = findViewById<ProgressBar>(R.id.progressQueue)

        adapter = HelpRequestAdapter(HelpRequestRowStyle.ADMIN) { item ->
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
        loadAll(progress)
    }

    private fun loadAll(progress: ProgressBar) {
        if (auth.currentUser == null) {
            progress.visibility = View.GONE
            Toast.makeText(this, R.string.responder_profile_need_login, Toast.LENGTH_LONG).show()
            finish()
            return
        }
        db.collection("helpRequests")
            .get()
            .addOnSuccessListener { snap ->
                val list = snap.documents.mapNotNull { HelpRequest.from(it) }
                    .sortedByDescending { it.createdAtMillis }
                adapter.submitList(list)
                progress.visibility = View.GONE
            }
            .addOnFailureListener { e ->
                progress.visibility = View.GONE
                Toast.makeText(this, getString(R.string.help_list_load_error, e.message ?: ""), Toast.LENGTH_LONG).show()
            }
    }
}
