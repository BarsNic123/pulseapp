package com.example.pulse

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.TextView
import androidx.appcompat.app.AppCompatActivity
import androidx.core.widget.NestedScrollView
import com.google.android.material.button.MaterialButton
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AdminDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin)

        findViewById<TextView>(R.id.tvHelloUser).text = getString(R.string.hello_admin_placeholder)
        findViewById<TextView>(R.id.tvRoleBadge).text = getString(R.string.role_badge_admin)

        findViewById<MaterialButton>(R.id.btnLogoutDashboard).setOnClickListener {
            AuthUi.logoutAndGoLogin(this)
        }

        val fab = findViewById<FloatingActionButton>(R.id.fabAddCase)
        val navDashboard = findViewById<LinearLayout>(R.id.navDashboard)
        val navCases = findViewById<LinearLayout>(R.id.navCases)
        val navTasks = findViewById<LinearLayout>(R.id.navTasks)
        val navTransparency = findViewById<LinearLayout>(R.id.navTransparency)
        val navResponders = findViewById<LinearLayout>(R.id.navResponders)
        val navProfile = findViewById<LinearLayout>(R.id.navProfile)
        val scrollContent = findViewById<NestedScrollView>(R.id.scrollContent)

        fab?.setOnClickListener {
            startActivity(
                Intent(this, PlaceholderActivity::class.java)
                    .putExtra("EXTRA_TITLE", "Create Case")
                    .putExtra("EXTRA_MESSAGE", "Create-case flow is not implemented in this prototype.")
            )
        }

        navDashboard?.setOnClickListener {
            scrollContent?.smoothScrollTo(0, 0)
        }

        navCases?.setOnClickListener {
            startActivity(Intent(this, PatientListActivity::class.java))
        }

        navTasks?.setOnClickListener {
            startActivity(
                Intent(this, PlaceholderActivity::class.java)
                    .putExtra("EXTRA_TITLE", "Tasks")
                    .putExtra("EXTRA_MESSAGE", "Task management is not implemented in this prototype.")
            )
        }

        navTransparency?.setOnClickListener {
            startActivity(Intent(this, TransparencyDashboardActivity::class.java))
        }

        navResponders?.setOnClickListener {
            startActivity(
                Intent(this, ResponderListActivity::class.java)
                    .putExtra(ResponderListActivity.EXTRA_ASSIGN_MODE, false)
            )
        }

        navProfile?.setOnClickListener {
            startActivity(
                Intent(this, PlaceholderActivity::class.java)
                    .putExtra("EXTRA_TITLE", "Admin Profile")
                    .putExtra("EXTRA_MESSAGE", "You are logged in as Admin.")
            )
        }
    }
}
