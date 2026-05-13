package com.example.pulse

import android.os.Bundle
import android.widget.ArrayAdapter
import android.widget.EditText
import android.widget.RadioGroup
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.appbar.MaterialToolbar
import com.google.android.material.button.MaterialButton
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class PatientEditActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_patient_edit)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        val targetUid = intent.getStringExtra(EXTRA_TARGET_UID) ?: auth.currentUser?.uid
        val editedByResponder = intent.getBooleanExtra(EXTRA_EDITED_BY_RESPONDER, false)

        if (targetUid.isNullOrBlank()) {
            Toast.makeText(this, R.string.responder_profile_need_login, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.patientEditRoot)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        findViewById<MaterialToolbar>(R.id.toolbarPatientEdit).setNavigationOnClickListener { finish() }

        val disclaimer = findViewById<TextView>(R.id.tvEditDisclaimer)
        if (editedByResponder) {
            disclaimer.visibility = android.view.View.VISIBLE
            disclaimer.text = getString(R.string.patient_edit_responder_hint)
        } else {
            disclaimer.visibility = android.view.View.VISIBLE
            disclaimer.text = getString(R.string.patient_edit_self_hint)
        }

        val spn = findViewById<Spinner>(R.id.spnBloodType)
        ArrayAdapter.createFromResource(this, R.array.blood_types, android.R.layout.simple_spinner_item).also { ad ->
            ad.setDropDownViewResource(android.R.layout.simple_spinner_dropdown_item)
            spn.adapter = ad
        }

        findViewById<MaterialButton>(R.id.btnSavePatient).setOnClickListener {
            save(targetUid, editedByResponder)
        }

        load(targetUid, spn)
    }

    private fun load(targetUid: String, spn: Spinner) {
        db.collection("users").document(targetUid).get()
            .addOnSuccessListener { doc ->
                findViewById<EditText>(R.id.etFullName).setText(doc.getString("fullName").orEmpty())
                findViewById<EditText>(R.id.etAge).setText(doc.getString("age").orEmpty())
                findViewById<EditText>(R.id.etEmergency).setText(doc.getString("emergencyContact").orEmpty())
                findViewById<EditText>(R.id.etMedicalNotes).setText(doc.getString("medicalNotes").orEmpty())
                val blood = doc.getString("bloodType").orEmpty()
                val adapter = spn.adapter as? ArrayAdapter<*>
                if (adapter != null) {
                    for (i in 0 until adapter.count) {
                        if (adapter.getItem(i)?.toString() == blood) {
                            spn.setSelection(i)
                            break
                        }
                    }
                }
                val risk = doc.getString("riskLevel").orEmpty()
                val rg = findViewById<RadioGroup>(R.id.rgRiskLevel)
                when {
                    risk.contains("High", ignoreCase = true) -> rg.check(R.id.rbHighRisk)
                    else -> rg.check(R.id.rbLowRisk)
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, getString(R.string.patient_edit_load_error, e.message ?: ""), Toast.LENGTH_LONG).show()
            }
    }

    private fun save(targetUid: String, editedByResponder: Boolean) {
        val name = findViewById<EditText>(R.id.etFullName).text.toString().trim()
        val age = findViewById<EditText>(R.id.etAge).text.toString().trim()
        val emergency = findViewById<EditText>(R.id.etEmergency).text.toString().trim()
        val notes = findViewById<EditText>(R.id.etMedicalNotes).text.toString().trim()
        val blood = findViewById<Spinner>(R.id.spnBloodType).selectedItem?.toString()?.trim().orEmpty()
        val rg = findViewById<RadioGroup>(R.id.rgRiskLevel)
        val risk = if (rg.checkedRadioButtonId == R.id.rbHighRisk) "High Risk" else "Low Risk"

        if (name.isEmpty() || age.isEmpty() || emergency.isEmpty() || blood.isEmpty()) {
            Toast.makeText(this, R.string.patient_edit_validation, Toast.LENGTH_LONG).show()
            return
        }

        if (!editedByResponder) {
            val self = auth.currentUser?.uid
            if (self == null || self != targetUid) {
                Toast.makeText(this, R.string.patient_edit_not_allowed, Toast.LENGTH_LONG).show()
                return
            }
        }

        val updates = hashMapOf<String, Any>(
            "fullName" to name,
            "age" to age,
            "bloodType" to blood,
            "emergencyContact" to emergency,
            "medicalNotes" to notes,
            "riskLevel" to risk
        )

        db.collection("users").document(targetUid).update(updates)
            .addOnSuccessListener {
                Toast.makeText(this, R.string.patient_edit_saved, Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, getString(R.string.patient_edit_save_error, e.message ?: ""), Toast.LENGTH_LONG).show()
            }
    }

    companion object {
        const val EXTRA_TARGET_UID = "EXTRA_TARGET_UID"
        const val EXTRA_EDITED_BY_RESPONDER = "EXTRA_EDITED_BY_RESPONDER"
    }
}
