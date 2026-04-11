package com.example.pulse

import android.os.Bundle
import androidx.appcompat.app.AppCompatActivity
import androidx.recyclerview.widget.LinearLayoutManager
import androidx.recyclerview.widget.RecyclerView

class ResponderListActivity : AppCompatActivity() {
    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_responder_list)

        val rvResponders = findViewById<RecyclerView>(R.id.rvResponders)
        rvResponders.layoutManager = LinearLayoutManager(this)
        rvResponders.adapter = ResponderAdapter(ResponderManager.list)
    }
}