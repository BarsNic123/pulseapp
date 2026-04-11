package com.example.pulse

import android.content.Intent
import android.os.Bundle
import android.widget.LinearLayout
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AdminDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin)

        val fab = findViewById<FloatingActionButton>(R.id.fabAddCase)
        val navDashboard = findViewById<LinearLayout>(R.id.navDashboard)
        val navCases = findViewById<LinearLayout>(R.id.navCases)
        val navTasks = findViewById<LinearLayout>(R.id.navTasks)
        val navResponders = findViewById<LinearLayout>(R.id.navResponders)
        val navProfile = findViewById<LinearLayout>(R.id.navProfile)

        fab?.setOnClickListener {
            Toast.makeText(this, "Create New Case Clicked", Toast.LENGTH_SHORT).show()
        }

        navDashboard?.setOnClickListener {
            Toast.makeText(this, "Dashboard Selected", Toast.LENGTH_SHORT).show()
        }

        navCases?.setOnClickListener {
            Toast.makeText(this, "Cases Selected", Toast.LENGTH_SHORT).show()
        }

        navTasks?.setOnClickListener {
            Toast.makeText(this, "Tasks Selected", Toast.LENGTH_SHORT).show()
        }

        navResponders?.setOnClickListener {
            val intent = Intent(this, ResponderListActivity::class.java)
            startActivity(intent)
        }

        navProfile?.setOnClickListener {
            Toast.makeText(this, "Profile Selected", Toast.LENGTH_SHORT).show()
        }
    }
}