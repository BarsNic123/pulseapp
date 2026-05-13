package com.example.pulse

import android.os.Bundle
import android.widget.TextView
import android.content.Intent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton

class TransparencyDashboardActivity : AppCompatActivity() {

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transparency_dashboard)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.transparencyRoot)) { v, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            v.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        findViewById<MaterialButton>(R.id.btnBackTransparency).setOnClickListener { finish() }
        findViewById<MaterialButton>(R.id.btnOpenTransactions).setOnClickListener {
            startActivity(Intent(this, TransactionActivity::class.java))
        }
        findViewById<MaterialButton>(R.id.btnOpenRevenue).setOnClickListener {
            startActivity(Intent(this, RevenueActivity::class.java))
        }

        val tvTotalBalance = findViewById<TextView>(R.id.tvTotalBalance)
        val total = FinanceSampleData.netBalance()
        tvTotalBalance.text = "₱${String.format("%,.2f", total)}"
    }
}
