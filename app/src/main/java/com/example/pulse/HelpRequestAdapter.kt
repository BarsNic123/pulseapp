package com.example.pulse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

class HelpRequestAdapter(
    private val showPatient: Boolean,
    private val onClick: (HelpRequest) -> Unit
) : ListAdapter<HelpRequest, HelpRequestAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_help_request, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position), showPatient, onClick)
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvHelpTitle)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tvHelpSubtitle)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvHelpStatus)

        fun bind(item: HelpRequest, showPatient: Boolean, onClick: (HelpRequest) -> Unit) {
            if (showPatient) {
                tvTitle.text = itemView.context.getString(R.string.help_row_responder_format, item.responderName.ifBlank { "—" })
                tvSubtitle.text = item.locationSummary.ifBlank { itemView.context.getString(R.string.help_row_no_location) }
            } else {
                tvTitle.text = item.patientName.ifBlank { itemView.context.getString(R.string.dashboard_name_fallback) }
                tvSubtitle.text = item.locationSummary.ifBlank { itemView.context.getString(R.string.help_row_no_location) }
            }
            tvStatus.text = item.statusLabel()
            itemView.setOnClickListener { onClick(item) }
        }
    }

    companion object {
        private val DIFF = object : DiffUtil.ItemCallback<HelpRequest>() {
            override fun areItemsTheSame(a: HelpRequest, b: HelpRequest) = a.id == b.id
            override fun areContentsTheSame(a: HelpRequest, b: HelpRequest) = a == b
        }
    }
}
