package com.example.pulse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class MedicineAdapter(private var meds: List<Medicine>) : RecyclerView.Adapter<MedicineAdapter.ViewHolder>() {

    fun submitList(list: List<Medicine>) {
        meds = list
        notifyDataSetChanged()
    }

    class ViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val category: TextView = view.findViewById(R.id.tvMedCategory)
        val name: TextView = view.findViewById(R.id.tvMedName)
        val dosage: TextView = view.findViewById(R.id.tvMedDosage)
        val time: TextView = view.findViewById(R.id.tvMedTime)
        val instructions: TextView = view.findViewById(R.id.tvMedInstructions)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_medicine, parent, false)
        return ViewHolder(view)
    }

    override fun onBindViewHolder(holder: ViewHolder, position: Int) {
        val med = meds[position]
        holder.category.text = med.category
        holder.name.text = med.name
        holder.dosage.text = med.dosage
        if (med.time.isBlank()) {
            holder.time.visibility = View.GONE
        } else {
            holder.time.visibility = View.VISIBLE
            holder.time.text = med.time
        }
        holder.instructions.text = med.instructions
    }

    override fun getItemCount() = meds.size
}
