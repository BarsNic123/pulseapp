package com.example.pulse

import android.content.Intent
import android.os.Bundle
import android.widget.TextView
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class TransparencyDashboardActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private var financeListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transparency_dashboard)

        db = FirebaseFirestore.getInstance()

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
    }

    override fun onStart() {
        super.onStart()
        val tvTotalBalance = findViewById<TextView>(R.id.tvTotalBalance)
        financeListener = FinanceFirestore.listen(db) { list ->
            val total = FinanceFirestore.netBalance(list)
            tvTotalBalance.text = "₱${String.format("%,.2f", total)}"
        }
    }

    override fun onStop() {
        super.onStop()
        financeListener?.remove()
        financeListener = null
    }
}
