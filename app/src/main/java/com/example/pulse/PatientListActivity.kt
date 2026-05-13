package com.example.pulse

import android.os.Bundle
import android.util.Log
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ProgressBar
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.appbar.MaterialToolbar
import com.google.firebase.firestore.FirebaseFirestore

private data class PatientRow(
    val uid: String,
    val name: String,
    val email: String,
    val riskLevel: String
)

private class PatientDirectoryAdapter(
    private val items: MutableList<PatientRow> = mutableListOf()
) : RecyclerView.Adapter<PatientDirectoryAdapter.VH>() {

    class VH(view: View) : RecyclerView.ViewHolder(view) {
        val name: TextView = view.findViewById(R.id.tvPatientName)
        val email: TextView = view.findViewById(R.id.tvPatientEmail)
        val risk: TextView = view.findViewById(R.id.tvPatientRisk)
    }

    fun submitList(list: List<PatientRow>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_patient_directory, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        val p = items[position]
        holder.name.text = p.name
        holder.email.text = p.email
        holder.risk.text = holder.itemView.context.getString(R.string.patient_row_risk_format, p.riskLevel)
    }

    override fun getItemCount() = items.size
}

class PatientListActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var adapter: PatientDirectoryAdapter

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_patient_list)

        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.patientListRoot)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        val toolbar = findViewById<MaterialToolbar>(R.id.toolbarPatients)
        toolbar.setNavigationOnClickListener { finish() }
        toolbar.title = getString(R.string.patient_directory_title)

        val rv = findViewById<RecyclerView>(R.id.rvPatients)
        val progress = findViewById<ProgressBar>(R.id.progressPatients)
        adapter = PatientDirectoryAdapter()
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        progress.visibility = View.VISIBLE
        db.collection("users").whereEqualTo("role", "Patient").get()
            .addOnSuccessListener { snap ->
                progress.visibility = View.GONE
                val rows = snap.documents.map { doc ->
                    PatientRow(
                        uid = doc.id,
                        name = doc.getString("fullName").orEmpty().ifBlank { doc.getString("email").orEmpty() },
                        email = doc.getString("email").orEmpty(),
                        riskLevel = doc.getString("riskLevel").orEmpty().ifBlank { "—" }
                    )
                }
                adapter.submitList(rows)
                if (rows.isEmpty()) {
                    Toast.makeText(this, R.string.patient_list_empty, Toast.LENGTH_LONG).show()
                }
            }
            .addOnFailureListener { e ->
                Log.w(TAG, "Patient directory query failed", e)
                progress.visibility = View.GONE
                adapter.submitList(emptyList())
                Toast.makeText(this, R.string.patient_list_cloud_unavailable, Toast.LENGTH_LONG).show()
            }
    }

    companion object {
        private const val TAG = "PatientList"
    }
}
