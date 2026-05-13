package com.example.pulse

import android.graphics.Color
import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import android.widget.Button
import android.widget.EditText
import android.widget.Spinner
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.button.MaterialButton
import com.google.android.material.materialswitch.MaterialSwitch
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.SetOptions

class ResponderEditActivity : AppCompatActivity() {

    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    private lateinit var etName: EditText
    private lateinit var etLicense: EditText
    private lateinit var etBio: EditText
    private lateinit var spnExpertise: Spinner
    private lateinit var spnAffiliationCategory: Spinner
    private lateinit var spnAffiliationInstitution: Spinner
    private lateinit var switchActive: MaterialSwitch
    private lateinit var tvDesc: TextView

    private val roles = arrayOf("Doctor", "Nurse", "MedTech", "Medical Student", "First Aider")
    private val affiliationCategories = listOf(CebuInstitutions.TYPE_HOSPITAL, CebuInstitutions.TYPE_UNIVERSITY)

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_responder_edit)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()

        etName = findViewById(R.id.etName)
        etLicense = findViewById(R.id.etLicense)
        etBio = findViewById(R.id.etBio)
        spnExpertise = findViewById(R.id.spnExpertise)
        spnAffiliationCategory = findViewById(R.id.spnAffiliationCategory)
        spnAffiliationInstitution = findViewById(R.id.spnAffiliationInstitution)
        switchActive = findViewById(R.id.switchActiveStatus)
        tvDesc = findViewById(R.id.tvStatusDescription)

        spnExpertise.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            roles
        )
        spnAffiliationCategory.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            affiliationCategories
        )
        spnAffiliationCategory.onItemSelectedListener = object : AdapterView.OnItemSelectedListener {
            override fun onItemSelected(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
                reloadAffiliationInstitutions()
            }

            override fun onNothingSelected(parent: AdapterView<*>?) {}
        }
        reloadAffiliationInstitutions()

        switchActive.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                tvDesc.text = "ONLINE - READY"
                tvDesc.setTextColor(Color.parseColor("#0B5A7A"))
            } else {
                tvDesc.text = "OFFLINE - SILENCED"
                tvDesc.setTextColor(Color.parseColor("#9E9E9E"))
            }
        }

        findViewById<MaterialButton>(R.id.btnBack).setOnClickListener { finish() }
        findViewById<Button>(R.id.btnSave).setOnClickListener { saveProfile() }

        loadFromFirestore()
    }

    private fun institutionsForSelectedCategory(): List<String> {
        val type = spnAffiliationCategory.selectedItem?.toString() ?: CebuInstitutions.TYPE_HOSPITAL
        return if (type == CebuInstitutions.TYPE_UNIVERSITY) {
            CebuInstitutions.universitiesCebuCity
        } else {
            CebuInstitutions.hospitalsCebuCity
        }
    }

    private fun reloadAffiliationInstitutions() {
        val items = institutionsForSelectedCategory()
        spnAffiliationInstitution.adapter = ArrayAdapter(
            this,
            android.R.layout.simple_spinner_dropdown_item,
            items
        )
    }

    private fun loadFromFirestore() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, R.string.responder_profile_need_login, Toast.LENGTH_LONG).show()
            finish()
            return
        }

        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                etName.setText(doc.getString("fullName").orEmpty())
                etLicense.setText(doc.getString("license").orEmpty())
                etBio.setText(doc.getString("bio").orEmpty())

                val exp = doc.getString("expertise").orEmpty()
                val expIdx = roles.indexOfFirst { it.equals(exp, ignoreCase = true) }
                if (expIdx >= 0) spnExpertise.setSelection(expIdx)

                applyAffiliationFromDoc(doc)

                val online = doc.getBoolean("availabilityOnline") ?: true
                switchActive.isChecked = online
                tvDesc.text = if (online) "ONLINE - READY" else "OFFLINE - SILENCED"
                tvDesc.setTextColor(
                    Color.parseColor(if (online) "#0B5A7A" else "#9E9E9E")
                )
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, getString(R.string.profile_load_error, e.message ?: ""), Toast.LENGTH_LONG).show()
            }
    }

    private fun applyAffiliationFromDoc(doc: DocumentSnapshot) {
        var cat = doc.getString("affiliationType").orEmpty()
        var place = doc.getString("affiliatedInstitution").orEmpty()
        val org = doc.getString("organization").orEmpty()
        val ref = place.ifBlank { org }

        if (cat != CebuInstitutions.TYPE_HOSPITAL && cat != CebuInstitutions.TYPE_UNIVERSITY) {
            cat = when {
                ref.isNotBlank() && CebuInstitutions.hospitalsCebuCity.any { it.equals(ref, ignoreCase = true) } ->
                    CebuInstitutions.TYPE_HOSPITAL
                ref.isNotBlank() && CebuInstitutions.universitiesCebuCity.any { it.equals(ref, ignoreCase = true) } ->
                    CebuInstitutions.TYPE_UNIVERSITY
                org.isNotBlank() && CebuInstitutions.hospitalsCebuCity.any { it.equals(org, ignoreCase = true) } -> {
                    if (place.isBlank()) place = org
                    CebuInstitutions.TYPE_HOSPITAL
                }
                org.isNotBlank() && CebuInstitutions.universitiesCebuCity.any { it.equals(org, ignoreCase = true) } -> {
                    if (place.isBlank()) place = org
                    CebuInstitutions.TYPE_UNIVERSITY
                }
                else -> CebuInstitutions.TYPE_HOSPITAL
            }
        }
        if (place.isBlank() && ref.isNotBlank()) place = ref

        val catIdx = affiliationCategories.indexOf(cat).coerceAtLeast(0)
        spnAffiliationCategory.setSelection(catIdx)
        reloadAffiliationInstitutions()

        val items = institutionsForSelectedCategory()
        val instIdx = items.indexOfFirst { it.equals(place, ignoreCase = true) }
            .takeIf { it >= 0 } ?: 0
        spnAffiliationInstitution.setSelection(instIdx.coerceAtMost((spnAffiliationInstitution.adapter?.count ?: 1) - 1))
    }

    private fun saveProfile() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, R.string.responder_profile_need_login, Toast.LENGTH_LONG).show()
            return
        }

        val name = etName.text.toString().trim()
        val license = etLicense.text.toString().trim()
        val expertise = spnExpertise.selectedItem?.toString().orEmpty()
        val affType = spnAffiliationCategory.selectedItem?.toString().orEmpty()
        val institution = spnAffiliationInstitution.selectedItem?.toString().orEmpty()
        val bio = etBio.text.toString().trim()

        if (name.isEmpty() || license.isEmpty() || institution.isEmpty()) {
            Toast.makeText(this, R.string.responder_edit_validation, Toast.LENGTH_LONG).show()
            return
        }

        val updates = hashMapOf<String, Any>(
            "fullName" to name,
            "expertise" to expertise,
            "license" to license,
            "affiliationType" to affType,
            "affiliatedInstitution" to institution,
            "organization" to institution,
            "bio" to bio,
            "availabilityOnline" to switchActive.isChecked
        )

        findViewById<Button>(R.id.btnSave).isEnabled = false
        db.collection("users").document(uid).set(updates, SetOptions.merge())
            .addOnSuccessListener {
                Toast.makeText(this, R.string.responder_edit_saved, Toast.LENGTH_SHORT).show()
                finish()
            }
            .addOnFailureListener { e ->
                findViewById<Button>(R.id.btnSave).isEnabled = true
                Toast.makeText(this, getString(R.string.responder_edit_save_error, e.message ?: ""), Toast.LENGTH_LONG).show()
            }
    }
}
