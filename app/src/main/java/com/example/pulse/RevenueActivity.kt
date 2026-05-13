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
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class RevenueActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var rv: RecyclerView
    private lateinit var tvRevenueTotal: TextView
    private var financeListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_revenue)

        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.revenueRoot)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        findViewById<MaterialButton>(R.id.btnBackRevenue).setOnClickListener { finish() }

        tvRevenueTotal = findViewById(R.id.tvRevenueTotal)
        rv = findViewById(R.id.rvRevenueList)
        rv.layoutManager = LinearLayoutManager(this)
    }

    override fun onStart() {
        super.onStart()
        financeListener = FinanceFirestore.listen(db) { list ->
            tvRevenueTotal.text = "₱${String.format("%,.2f", FinanceFirestore.revenueTotal(list))}"
            val incomeOnly = list.filter { it.type == "INCOME" }
            rv.adapter = TransactionAdapter(incomeOnly)
        }
    }

    override fun onStop() {
        super.onStop()
        financeListener?.remove()
        financeListener = null
    }
}
