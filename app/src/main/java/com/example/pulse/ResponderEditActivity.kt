package com.example.pulse

import android.content.Intent
import android.graphics.Color
import android.os.Bundle
import android.widget.Button
import android.widget.EditText
import android.widget.TextView
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.chip.Chip
import com.google.android.material.chip.ChipGroup
import com.google.android.material.materialswitch.MaterialSwitch

class ResponderEditActivity : AppCompatActivity() {
    private var originalName: String? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_responder_edit)

        val switchStatus = findViewById<MaterialSwitch>(R.id.switchActiveStatus)
        val tvDesc = findViewById<TextView>(R.id.tvStatusDescription)
        val etName = findViewById<EditText>(R.id.etName)
        val etAge = findViewById<EditText>(R.id.etAge)
        val etBio = findViewById<EditText>(R.id.etBio)
        val cgExpertise = findViewById<ChipGroup>(R.id.cgExpertise)
        val btnSave = findViewById<Button>(R.id.btnSave)
        val btnBack = findViewById<TextView>(R.id.btnBack)

        if (intent.getBooleanExtra("EDIT_MODE", false)) {
            originalName = intent.getStringExtra("USER_NAME")
            etName.setText(originalName)
            etAge.setText(intent.getStringExtra("USER_AGE"))
            etBio.setText(intent.getStringExtra("USER_BIO"))
            switchStatus.isChecked = (intent.getStringExtra("USER_STATUS") == "ONLINE")

            val expertise = intent.getStringArrayListExtra("USER_EXPERTISE")
            expertise?.forEach { skill ->
                for (i in 0 until cgExpertise.childCount) {
                    val chip = cgExpertise.getChildAt(i) as Chip
                    if (chip.text.toString() == skill) chip.isChecked = true
                }
            }
            btnSave.text = "UPDATE PROFILE"
        }

        switchStatus.setOnCheckedChangeListener { _, isChecked ->
            if (isChecked) {
                tvDesc.text = "ONLINE - READY"
                tvDesc.setTextColor(Color.parseColor("#1A73E8"))
            } else {
                tvDesc.text = "OFFLINE - SILENCED"
                tvDesc.setTextColor(Color.parseColor("#9E9E9E"))
            }
        }

        btnBack.setOnClickListener {
            finish()
        }

        btnSave.setOnClickListener {
            val name = etName.text.toString()
            val age = etAge.text.toString()
            val bio = etBio.text.toString()
            val status = if (switchStatus.isChecked) "ONLINE" else "OFFLINE"

            val selectedChips = mutableListOf<String>()
            for (i in 0 until cgExpertise.childCount) {
                val chip = cgExpertise.getChildAt(i) as Chip
                if (chip.isChecked) selectedChips.add(chip.text.toString())
            }

            if (name.isNotEmpty() && age.isNotEmpty()) {
                val updated = Responder(name, age, status, selectedChips, bio)
                ResponderManager.updateOrAdd(originalName, updated)

                val intent = Intent(this, ResponderDashboardActivity::class.java)
                intent.flags = Intent.FLAG_ACTIVITY_CLEAR_TOP
                startActivity(intent)
                finish()
            } else {
                Toast.makeText(this, "Please fill in all fields", Toast.LENGTH_SHORT).show()
            }
        }
    }
}