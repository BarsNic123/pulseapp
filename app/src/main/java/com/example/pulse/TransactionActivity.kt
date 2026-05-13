package com.example.pulse

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton

class TransactionActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transaction)

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.transactionRoot)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        findViewById<MaterialButton>(R.id.btnBackTransaction).setOnClickListener { finish() }

        val rv = findViewById<RecyclerView>(R.id.rvTransactionList)
        rv.layoutManager = LinearLayoutManager(this)
        rv.adapter = TransactionAdapter(FinanceSampleData.transactions)
    }
}
