package com.example.pulse

import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ProgressBar
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AlertDialog
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class ResponderListActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: ResponderAdapter

    private val assignMode: Boolean by lazy {
        intent.getBooleanExtra(EXTRA_ASSIGN_MODE, true)
    }

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_responder_list)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.responderListRoot)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarResponders)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.title = if (assignMode) {
            getString(R.string.responder_directory_assign)
        } else {
            getString(R.string.responder_directory_browse)
        }

        val rv = findViewById<RecyclerView>(R.id.rvResponders)
        val progress = findViewById<ProgressBar>(R.id.progressResponders)

        adapter = ResponderAdapter(assignMode) { responder ->
            if (assignMode) confirmAssign(responder) else showResponderDetails(responder)
        }
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        progress.visibility = View.VISIBLE
        fetchResponders { list ->
            progress.visibility = View.GONE
            adapter.submitList(list)
            if (list.isEmpty()) {
                Toast.makeText(this, R.string.responder_list_empty, Toast.LENGTH_LONG).show()
            }
        }
    }

    private fun fetchResponders(onDone: (List<Responder>) -> Unit) {
        db.collection("users")
            .whereIn("role", listOf("Respondent", "Responder"))
            .get()
            .addOnSuccessListener { snap ->
                val fromCloud = snap.documents.map { doc ->
                    val expertiseStr = doc.getString("expertise").orEmpty()
                    val org = doc.getString("organization").orEmpty()
                    val license = doc.getString("license").orEmpty()
                    val bioParts = listOf(org, license).filter { it.isNotBlank() }
                    Responder(
                        uid = doc.id,
                        name = doc.getString("fullName").orEmpty().ifBlank { "Responder" },
                        age = doc.getString("age").orEmpty().ifBlank { "—" },
                        status = getString(R.string.responder_status_registered),
                        expertise = if (expertiseStr.isNotBlank()) listOf(expertiseStr) else emptyList(),
                        bio = bioParts.joinToString(" · ").ifBlank { doc.getString("email").orEmpty() }
                    )
                }
                onDone(fromCloud)
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Responder directory query failed", e)
                onDone(emptyList())
                Toast.makeText(this, R.string.responder_list_cloud_unavailable, Toast.LENGTH_LONG).show()
            }
    }

    private fun confirmAssign(responder: Responder) {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, R.string.responder_assign_need_login, Toast.LENGTH_LONG).show()
            return
        }
        val expertise = responder.expertise.joinToString(", ").ifEmpty { "—" }
        AlertDialog.Builder(this)
            .setTitle(R.string.responder_list_assign_title)
            .setMessage(
                getString(
                    R.string.responder_assign_confirm_message,
                    responder.name,
                    expertise
                )
            )
            .setNegativeButton(android.R.string.cancel, null)
            .setPositiveButton(R.string.responder_assign_confirm) { _, _ ->
                val updates = hashMapOf<String, Any>(
                    "assignedResponderName" to responder.name,
                    "assignedResponderExpertise" to expertise,
                    "assignedResponderUid" to (responder.uid ?: ""),
                    "assignedAt" to FieldValue.serverTimestamp()
                )
                db.collection("users").document(uid).update(updates)
                    .addOnSuccessListener {
                        Toast.makeText(
                            this,
                            getString(R.string.responder_assigned_success, responder.name),
                            Toast.LENGTH_LONG
                        ).show()
                        finish()
                    }
                    .addOnFailureListener { e ->
                        Toast.makeText(
                            this,
                            getString(R.string.responder_assigned_error, e.message ?: ""),
                            Toast.LENGTH_LONG
                        ).show()
                    }
            }
            .show()
    }

    private fun showResponderDetails(responder: Responder) {
        val body = buildString {
            appendLine(responder.status)
            appendLine()
            appendLine(getString(R.string.responder_expertise_line, responder.expertise.joinToString(", ").ifEmpty { "—" }))
            appendLine()
            append(if (responder.bio.isBlank()) getString(R.string.responder_no_bio) else responder.bio)
            responder.uid?.let {
                appendLine()
                appendLine()
                append(getString(R.string.responder_uid_line, it))
            }
        }
        AlertDialog.Builder(this)
            .setTitle(responder.name)
            .setMessage(body)
            .setPositiveButton(android.R.string.ok, null)
            .show()
    }

    companion object {
        const val EXTRA_ASSIGN_MODE = "EXTRA_ASSIGN_MODE"
        private const val TAG = "ResponderList"
    }
}
