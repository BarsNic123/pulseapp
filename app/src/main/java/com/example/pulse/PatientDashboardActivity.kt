package com.example.pulse

import android.Manifest
import android.content.Intent
import android.content.pm.PackageManager
import android.location.Location
import android.os.Bundle
import android.view.View
import android.widget.Button
import android.widget.LinearLayout
import android.widget.ScrollView
import android.widget.TextView
import android.widget.Toast
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.gms.location.FusedLocationProviderClient
import com.google.android.gms.location.LocationServices
import com.google.android.material.button.MaterialButton
import com.google.android.material.card.MaterialCardView
import com.google.android.material.switchmaterial.SwitchMaterial
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.FieldValue
import com.google.firebase.firestore.FirebaseFirestore

class PatientDashboardActivity : AppCompatActivity() {

    private val permissionRequestCode = 101
    private lateinit var fusedLocationClient: FusedLocationProviderClient
    private lateinit var auth: FirebaseAuth
    private lateinit var db: FirebaseFirestore

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_patient_dashboard)

        auth = FirebaseAuth.getInstance()
        db = FirebaseFirestore.getInstance()
        fusedLocationClient = LocationServices.getFusedLocationProviderClient(this)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(android.R.id.content)) { v, insets ->
            val systemBars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(0, 0, 0, systemBars.bottom)
            insets
        }

        findViewById<MaterialButton>(R.id.btnLogoutDashboard).setOnClickListener {
            AuthUi.logoutAndGoLogin(this)
        }

        findViewById<TextView>(R.id.tvRoleBadge).text = getString(R.string.role_badge_patient)

        val cvRecordVoice = findViewById<MaterialCardView>(R.id.cvRecordVoice)
        val cvRequestRide = findViewById<MaterialCardView>(R.id.cvRequestRide)
        val cvMyMedicine = findViewById<MaterialCardView>(R.id.cvMyMedicine)
        val btnSos = findViewById<Button>(R.id.btnSos)
        val switchAutoSafe = findViewById<SwitchMaterial>(R.id.switchAutoSafe)

        val navDashboard = findViewById<LinearLayout>(R.id.navDashboard)
        val navHelpList = findViewById<LinearLayout>(R.id.navHelpList)
        val navProfile = findViewById<LinearLayout>(R.id.navProfile)
        val scrollContent = findViewById<ScrollView>(R.id.scrollContent)

        findViewById<TextView>(R.id.tvVoiceHistoryLink).setOnClickListener {
            startActivity(Intent(this, VoiceNotesHistoryActivity::class.java))
        }

        val cvHelpDoneBanner = findViewById<MaterialCardView>(R.id.cvHelpDoneBanner)
        cvHelpDoneBanner.setOnClickListener {
            startActivity(Intent(this, PatientHelpListActivity::class.java))
        }

        cvRecordVoice?.setOnClickListener {
            if (checkPermission(Manifest.permission.RECORD_AUDIO)) {
                startActivity(Intent(this, VoiceRecorderActivity::class.java))
            } else {
                requestPermission(Manifest.permission.RECORD_AUDIO)
            }
        }

        cvRequestRide?.setOnClickListener {
            startActivity(
                Intent(this, ResponderListActivity::class.java)
                    .putExtra(ResponderListActivity.EXTRA_ASSIGN_MODE, true)
            )
        }

        cvMyMedicine?.setOnClickListener {
            startActivity(Intent(this, MedicineListActivity::class.java))
        }

        findViewById<MaterialCardView>(R.id.cvPatientTransactions).setOnClickListener {
            startActivity(Intent(this, TransactionActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.cvPatientRevenue).setOnClickListener {
            startActivity(Intent(this, RevenueActivity::class.java))
        }
        findViewById<MaterialCardView>(R.id.cvPatientTransparencyOverview).setOnClickListener {
            startActivity(Intent(this, TransparencyDashboardActivity::class.java))
        }

        btnSos?.setOnClickListener {
            triggerSosAlert()
        }

        switchAutoSafe?.setOnCheckedChangeListener { _, isChecked ->
            val status = if (isChecked) "enabled" else "disabled"
            Toast.makeText(this, "Auto safe text $status", Toast.LENGTH_SHORT).show()
        }

        navDashboard?.setOnClickListener {
            scrollContent?.smoothScrollTo(0, 0)
        }

        navHelpList?.setOnClickListener {
            startActivity(Intent(this, PatientHelpListActivity::class.java))
        }

        navProfile?.setOnClickListener {
            startActivity(Intent(this, PatientProfileActivity::class.java))
        }

        refreshPatientHeader()
    }

    override fun onResume() {
        super.onResume()
        refreshPatientHeader()
        refreshDoneBanner()
    }

    private fun firstName(full: String): String {
        val t = full.trim()
        if (t.isEmpty()) return getString(R.string.dashboard_name_fallback)
        return t.split(Regex("\\s+")).firstOrNull() ?: t
    }

    private fun refreshPatientHeader() {
        val tvHello = findViewById<TextView>(R.id.tvHelloUser)
        val tvAssigned = findViewById<TextView>(R.id.tvAssignedResponder)
        val uid = auth.currentUser?.uid
        if (uid == null) {
            tvHello?.text = getString(R.string.hello_user_placeholder)
            tvAssigned?.text = getString(R.string.assigned_none)
            return
        }
        db.collection("users").document(uid).get()
            .addOnSuccessListener { doc ->
                val name = doc.getString("fullName") ?: getString(R.string.dashboard_name_fallback)
                tvHello?.text = getString(R.string.hello_user_format, firstName(name))
                val assignedName = doc.getString("assignedResponderName")?.trim().orEmpty()
                tvAssigned?.text = if (assignedName.isEmpty()) {
                    getString(R.string.assigned_none)
                } else {
                    getString(R.string.assigned_prefix, assignedName)
                }
            }
            .addOnFailureListener {
                tvHello?.text = getString(R.string.hello_user_placeholder)
                tvAssigned?.text = getString(R.string.assigned_none)
            }
    }

    private fun refreshDoneBanner() {
        val uid = auth.currentUser?.uid ?: return
        val banner = findViewById<MaterialCardView>(R.id.cvHelpDoneBanner)
        val tv = findViewById<TextView>(R.id.tvHelpDoneBanner)
        db.collection("helpRequests")
            .whereEqualTo("patientUid", uid)
            .get()
            .addOnSuccessListener { snap ->
                val unseen = snap.documents.mapNotNull { HelpRequest.from(it) }
                    .count { it.status == HelpRequestStatus.DONE && !it.patientSeenCompletion }
                if (unseen > 0) {
                    tv.text = resources.getQuantityString(R.plurals.help_done_banner, unseen, unseen)
                    banner.visibility = View.VISIBLE
                } else {
                    banner.visibility = View.GONE
                }
            }
            .addOnFailureListener {
                banner.visibility = View.GONE
            }
    }

    private fun triggerSosAlert() {
        val uid = auth.currentUser?.uid
        if (uid == null) {
            Toast.makeText(this, R.string.responder_assign_need_login, Toast.LENGTH_LONG).show()
            return
        }
        if (!checkPermission(Manifest.permission.ACCESS_FINE_LOCATION)) {
            requestPermission(Manifest.permission.ACCESS_FINE_LOCATION)
            return
        }
        db.collection("users").document(uid).get()
            .addOnSuccessListener { userDoc ->
                val responderUid = userDoc.getString("assignedResponderUid").orEmpty().trim()
                if (responderUid.isEmpty()) {
                    Toast.makeText(this, R.string.help_need_responder, Toast.LENGTH_LONG).show()
                    return@addOnSuccessListener
                }
                val responderName = userDoc.getString("assignedResponderName").orEmpty()
                try {
                    fusedLocationClient.lastLocation.addOnSuccessListener { location: Location? ->
                        val locationSummary = if (location != null) {
                            "Lat ${location.latitude}, Lon ${location.longitude}"
                        } else {
                            getString(R.string.help_location_unknown)
                        }
                        val voiceUrl = PulsePrefs.getLastVoiceUrl(this)
                        val help = hashMapOf(
                            "patientUid" to uid,
                            "patientName" to userDoc.getString("fullName").orEmpty().ifBlank { getString(R.string.dashboard_name_fallback) },
                            "patientAge" to userDoc.getString("age").orEmpty(),
                            "patientBloodType" to userDoc.getString("bloodType").orEmpty(),
                            "patientEmergency" to userDoc.getString("emergencyContact").orEmpty(),
                            "patientRiskLevel" to userDoc.getString("riskLevel").orEmpty(),
                            "patientMedicalNotes" to userDoc.getString("medicalNotes").orEmpty(),
                            "responderUid" to responderUid,
                            "responderName" to responderName,
                            "status" to HelpRequestStatus.PENDING,
                            "locationSummary" to locationSummary,
                            "createdAt" to FieldValue.serverTimestamp(),
                            "patientSeenCompletion" to true
                        )
                        if (!voiceUrl.isNullOrBlank()) {
                            help["voiceUrl"] = voiceUrl
                        }
                        db.collection("helpRequests").add(help)
                            .addOnSuccessListener {
                                PulsePrefs.saveLastVoiceUrl(this@PatientDashboardActivity, null)
                                Toast.makeText(this@PatientDashboardActivity, R.string.help_sent_ok, Toast.LENGTH_LONG).show()
                            }
                            .addOnFailureListener { e ->
                                Toast.makeText(this, getString(R.string.help_sent_error, e.message ?: ""), Toast.LENGTH_LONG).show()
                            }
                    }
                } catch (e: SecurityException) {
                    Toast.makeText(this, "Location permission error", Toast.LENGTH_SHORT).show()
                }
            }
            .addOnFailureListener { e ->
                Toast.makeText(this, getString(R.string.help_sent_error, e.message ?: ""), Toast.LENGTH_LONG).show()
            }
    }

    private fun checkPermission(permission: String): Boolean {
        return ContextCompat.checkSelfPermission(this, permission) == PackageManager.PERMISSION_GRANTED
    }

    private fun requestPermission(permission: String) {
        ActivityCompat.requestPermissions(this, arrayOf(permission), permissionRequestCode)
    }
}
