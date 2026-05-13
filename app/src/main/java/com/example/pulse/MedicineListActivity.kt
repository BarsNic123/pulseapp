package com.example.pulse

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FirebaseFirestore

class MedicineListActivity : AppCompatActivity() {

    private lateinit var adapter: MedicineAdapter
    private var allMedicines: List<Medicine> = emptyList()

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_medicine_list)

        findViewById<MaterialButton>(R.id.btnBack).setOnClickListener { finish() }

        val rv = findViewById<RecyclerView>(R.id.rvMedicineList)
        adapter = MedicineAdapter(emptyList())
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = adapter

        val chipGroup = findViewById<ChipGroup>(R.id.chipGroupMedicineCategory)
        val chipPairs = listOf(
            R.id.chipMedAll to getString(R.string.med_cat_all),
            R.id.chipMedGeneral to getString(R.string.med_cat_general),
            R.id.chipMedProfile to getString(R.string.med_cat_profile),
            R.id.chipMedAsNeeded to getString(R.string.med_cat_as_needed)
        )
        chipPairs.forEach { (id, label) ->
            findViewById<Chip>(id).setOnClickListener {
                chipGroup.check(id)
                filterByChip(label)
            }
        }

        loadRecommendations()
    }

    private fun loadRecommendations() {
        val uid = FirebaseAuth.getInstance().currentUser?.uid
        if (uid == null) {
            allMedicines = buildDefaultSuggestions("—", "")
            chipGroupDefaultAndFilter()
            Toast.makeText(this, R.string.medicine_need_login_hint, Toast.LENGTH_LONG).show()
            return
        }
        FirebaseFirestore.getInstance().collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val risk = doc.getString("riskLevel").orEmpty().ifBlank { "—" }
                val notes = doc.getString("medicalNotes").orEmpty()
                allMedicines = buildDefaultSuggestions(risk, notes)
                chipGroupDefaultAndFilter()
            }
            .addOnFailureListener {
                allMedicines = buildDefaultSuggestions("—", "")
                chipGroupDefaultAndFilter()
            }
    }

    private fun chipGroupDefaultAndFilter() {
        findViewById<ChipGroup>(R.id.chipGroupMedicineCategory).check(R.id.chipMedAll)
        filterByChip(getString(R.string.med_cat_all))
    }

    private fun buildDefaultSuggestions(riskLevel: String, medicalNotes: String): List<Medicine> {
        val n = medicalNotes.lowercase()
        val general = getString(R.string.med_cat_general)
        val profile = getString(R.string.med_cat_profile)
        val asNeeded = getString(R.string.med_cat_as_needed)
        val disclaimer = getString(R.string.medicine_education_disclaimer)

        val out = mutableListOf<Medicine>()

        out += Medicine(
            name = getString(R.string.med_suggest_hydration_title),
            dosage = getString(R.string.med_suggest_hydration_dose),
            time = "",
            instructions = disclaimer,
            category = general
        )

        if (riskLevel.contains("High", ignoreCase = true)) {
            out += Medicine(
                name = getString(R.string.med_suggest_high_risk_title),
                dosage = getString(R.string.med_suggest_high_risk_dose),
                time = "",
                instructions = getString(R.string.med_suggest_high_risk_note) + "\n" + disclaimer,
                category = profile
            )
        } else if (riskLevel.contains("Low", ignoreCase = true)) {
            out += Medicine(
                name = getString(R.string.med_suggest_low_risk_title),
                dosage = getString(R.string.med_suggest_low_risk_dose),
                time = "",
                instructions = disclaimer,
                category = profile
            )
        }

        if (n.contains("diabet") || n.contains("glucose") || n.contains("sugar") || n.contains("insulin")) {
            out += Medicine(
                name = getString(R.string.med_suggest_diabetes_title),
                dosage = getString(R.string.med_suggest_diabetes_dose),
                time = "",
                instructions = getString(R.string.med_suggest_diabetes_note) + "\n" + disclaimer,
                category = profile
            )
        }
        if (n.contains("blood pressure") || n.contains("hypertens") || n.contains("bp")) {
            out += Medicine(
                name = getString(R.string.med_suggest_bp_title),
                dosage = getString(R.string.med_suggest_bp_dose),
                time = "",
                instructions = getString(R.string.med_suggest_bp_note) + "\n" + disclaimer,
                category = profile
            )
        }
        if (n.contains("heart") || n.contains("cardiac") || n.contains("chest")) {
            out += Medicine(
                name = getString(R.string.med_suggest_cardiac_title),
                dosage = getString(R.string.med_suggest_cardiac_dose),
                time = "",
                instructions = getString(R.string.med_suggest_cardiac_note) + "\n" + disclaimer,
                category = profile
            )
        }
        if (n.contains("asthma") || n.contains("inhal")) {
            out += Medicine(
                name = getString(R.string.med_suggest_asthma_title),
                dosage = getString(R.string.med_suggest_asthma_dose),
                time = "",
                instructions = getString(R.string.med_suggest_asthma_note) + "\n" + disclaimer,
                category = profile
            )
        }
        if (n.contains("pain") || n.contains("fever") || n.contains("headache")) {
            out += Medicine(
                name = getString(R.string.med_suggest_analgesic_title),
                dosage = getString(R.string.med_suggest_analgesic_dose),
                time = "",
                instructions = getString(R.string.med_suggest_analgesic_note) + "\n" + disclaimer,
                category = asNeeded
            )
        }

        out += Medicine(
            name = getString(R.string.med_suggest_otc_title),
            dosage = getString(R.string.med_suggest_otc_dose),
            time = "",
            instructions = getString(R.string.med_suggest_otc_note) + "\n" + disclaimer,
            category = asNeeded
        )

        return out.distinctBy { "${it.name}|${it.category}" }
    }

    private fun filterByChip(label: String) {
        val allLabel = getString(R.string.med_cat_all)
        val filtered = if (label == allLabel) allMedicines
        else allMedicines.filter { it.category.equals(label, ignoreCase = true) }
        adapter.submitList(filtered)
    }
}
