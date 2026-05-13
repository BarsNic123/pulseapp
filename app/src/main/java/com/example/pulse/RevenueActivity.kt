package com.example.pulse

import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class RevenueActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_revenue)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.revenueRoot)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        findViewById<MaterialButton>(R.id.btnBackRevenue).setOnClickListener { finish() }

        val incomeOnly = FinanceSampleData.transactions.filter { it.type == "INCOME" }
        findViewById<TextView>(R.id.tvRevenueTotal).text =
            "₱${String.format("%,.2f", FinanceSampleData.revenueTotal())}"

        val rv = findViewById<RecyclerView>(R.id.rvRevenueList)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = TransactionAdapter(incomeOnly)
    }
}
