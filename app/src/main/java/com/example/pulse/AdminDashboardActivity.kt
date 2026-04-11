package com.example.pulse

import android.os.Bundle
import android.widget.Toast
import androidx.appcompat.app.AppCompatActivity
import com.google.android.material.bottomnavigation.BottomNavigationView
import com.google.android.material.floatingactionbutton.FloatingActionButton

class AdminDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.admin)

        // Android Studio will now be able to find these IDs
        val fab: FloatingActionButton = findViewById(R.id.fab_add_case)
        val bottomNav: BottomNavigationView = findViewById(R.id.bottom_navigation)

        fab.setOnClickListener {
            Toast.makeText(this, "Create New Case Clicked", Toast.LENGTH_SHORT).show()
        }

        bottomNav.setOnItemSelectedListener { item ->
            when (item.itemId) {
                R.id.nav_dashboard -> {
                    // Already here
                    true
                }
                R.id.nav_cases -> {
                    Toast.makeText(this, "Cases Selected", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_tasks -> {
                    Toast.makeText(this, "Tasks Selected", Toast.LENGTH_SHORT).show()
                    true
                }
                R.id.nav_profile -> {
                    Toast.makeText(this, "Profile Selected", Toast.LENGTH_SHORT).show()
                    true
                }
                else -> false
            }
        }
    }
}