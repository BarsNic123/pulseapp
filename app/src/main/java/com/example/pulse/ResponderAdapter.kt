package com.example.pulse

import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ResponderAdapter(
    private val assignMode: Boolean,
    private val onItemClick: (Responder) -> Unit
) : RecyclerView.Adapter<ResponderAdapter.ResponderViewHolder>() {

    private val items = mutableListOf<Responder>()

    fun submitList(list: List<Responder>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    class ResponderViewHolder(view: android.view.View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvItemName)
        val tvStatus: TextView = view.findViewById(R.id.tvItemStatus)
        val tvExpertise: TextView = view.findViewById(R.id.tvItemExpertise)
        val tvBio: TextView = view.findViewById(R.id.tvItemBio)
        val tvHint: TextView = view.findViewById(R.id.tvItemHint)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResponderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_responder, parent, false)
        return ResponderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResponderViewHolder, position: Int) {
        val responder = items[position]
        holder.tvName.text = "${responder.name}, ${responder.age}"
        holder.tvStatus.text = responder.status
        holder.tvExpertise.text = holder.itemView.context.getString(
            R.string.responder_expertise_line,
            responder.expertise.joinToString(", ").ifEmpty { "—" }
        )
        holder.tvBio.text = if (responder.bio.isEmpty()) {
            holder.itemView.context.getString(R.string.responder_no_bio)
        } else {
            responder.bio
        }
        holder.tvHint.text = if (assignMode) {
            holder.itemView.context.getString(R.string.responder_tap_assign)
        } else {
            holder.itemView.context.getString(R.string.responder_tap_details)
        }

        holder.itemView.setOnClickListener { onItemClick(responder) }
    }

    override fun getItemCount() = items.size
}
