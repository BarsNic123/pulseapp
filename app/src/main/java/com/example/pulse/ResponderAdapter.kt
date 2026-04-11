package com.example.pulse

import android.content.Intent
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.RecyclerView

class ResponderAdapter(private val responderList: List<Responder>) :
    RecyclerView.Adapter<ResponderAdapter.ResponderViewHolder>() {

    class ResponderViewHolder(view: View) : RecyclerView.ViewHolder(view) {
        val tvName: TextView = view.findViewById(R.id.tvItemName)
        val tvStatus: TextView = view.findViewById(R.id.tvItemStatus)
        val tvExpertise: TextView = view.findViewById(R.id.tvItemExpertise)
        val tvBio: TextView = view.findViewById(R.id.tvItemBio)
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ResponderViewHolder {
        val view = LayoutInflater.from(parent.context).inflate(R.layout.item_responder, parent, false)
        return ResponderViewHolder(view)
    }

    override fun onBindViewHolder(holder: ResponderViewHolder, position: Int) {
        val responder = responderList[position]
        holder.tvName.text = "${responder.name}, ${responder.age}"
        holder.tvStatus.text = responder.status
        holder.tvExpertise.text = "Expertise: ${responder.expertise.joinToString(", ")}"
        holder.tvBio.text = if (responder.bio.isEmpty()) "No bio provided." else responder.bio

        holder.itemView.setOnClickListener {
            val context = holder.itemView.context
            val intent = Intent(context, ResponderEditActivity::class.java).apply {
                putExtra("EDIT_MODE", true)
                putExtra("USER_NAME", responder.name)
                putExtra("USER_AGE", responder.age)
                putExtra("USER_STATUS", responder.status)
                putExtra("USER_BIO", responder.bio)
                putStringArrayListExtra("USER_EXPERTISE", ArrayList(responder.expertise))
            }
            context.startActivity(intent)
        }
    }

    override fun getItemCount() = responderList.size
}