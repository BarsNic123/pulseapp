package com.example.pulse

import android.os.Bundle
import android.widget.TextView
import android.content.Intent
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
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
        val rvTransactions = findViewById<RecyclerView>(R.id.rvTransactions)

        val transactionList = listOf(
            Transaction("1", "Donation: North Cebu Relief Fund", "May 01", 15000.00, "INCOME"),
            Transaction("2", "Purchased: First Aid Kit Batch A", "May 03", 2450.00, "EXPENSE"),
            Transaction("3", "Anonymous Donation", "May 05", 500.00, "INCOME"),
            Transaction("4", "Medical Supplies: Oxygen Tank", "May 07", 8500.00, "EXPENSE"),
            Transaction("5", "Community Health Fund Grant", "May 08", 20000.00, "INCOME")
        )

        val total = transactionList.sumOf { if (it.type == "INCOME") it.amount else -it.amount }
        tvTotalBalance.text = "₱${String.format("%,.2f", total)}"

        rvTransactions.layoutManager = LinearLayoutManager(this)
        rvTransactions.adapter = TransactionAdapter(transactionList)
    }
}
