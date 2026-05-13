package com.example.pulse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.ImageView
import android.widget.TextView
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView

class TransactionAdapter(private val transactions: List<Transaction>) :
    RecyclerView.Adapter<TransactionAdapter.ViewHolder>() {

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val icon: ImageView = view.findViewById(R.id.ivTransIcon)
        val title: TextView = view.findViewById(R.id.tvTransTitle)
        val date: TextView = view.findViewById(R.id.tvTransDate)
        val amount: TextView = view.findViewById(R.id.tvTransAmount)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_transaction, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val trans = transactions[position]
        val ctx = holder.itemView.context

        holder.title.text = trans.title
        holder.date.text = trans.date

        val isIncome = trans.type == "INCOME"
        holder.icon.setImageResource(
            if (isIncome) R.drawable.ic_trans_income else R.drawable.ic_trans_expense
        )

        val prefix = if (isIncome) "+ ₱" else "- ₱"
        holder.amount.text = "$prefix${String.format("%,.2f", trans.amount)}"
        holder.amount.setTextColor(
            ContextCompat.getColor(
                ctx,
                if (isIncome) R.color.pulse_success else R.color.pulse_error
            )
        )
    }

    override fun getItemCount() = transactions.size
}
