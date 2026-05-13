package com.example.pulse

import android.os.Bundle
import androidx.activity.enableEdgeToEdge
import androidx.appcompat.app.AppCompatActivity
import androidx.core.view.ViewCompat
import androidx.core.view.WindowInsetsCompat
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration

class TransactionActivity : AppCompatActivity() {

    private lateinit var db: FirebaseFirestore
    private lateinit var rv: RecyclerView
    private var financeListener: ListenerRegistration? = null

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        enableEdgeToEdge()
        setContentView(R.layout.activity_transaction)

        db = FirebaseFirestore.getInstance()

        ViewCompat.setOnApplyWindowInsetsListener(findViewById(R.id.transactionRoot)) { view, insets ->
            val bars = insets.getInsets(WindowInsetsCompat.Type.systemBars())
            view.setPadding(bars.left, bars.top, bars.right, bars.bottom)
            insets
        }

        findViewById<MaterialButton>(R.id.btnBackTransaction).setOnClickListener { finish() }

        rv = findViewById(R.id.rvTransactionList)
        rv.layoutManager = LinearLayoutManager(this)
    }

    override fun onStart() {
        super.onStart()
        financeListener = FinanceFirestore.listen(db) { list ->
            rv.adapter = TransactionAdapter(list)
        }
    }

    override fun onStop() {
        super.onStop()
        financeListener?.remove()
        financeListener = null
    }
}
