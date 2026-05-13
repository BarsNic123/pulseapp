package com.example.pulse

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.TextView
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.ListAdapter
import androidx.recyclerview.widget.RecyclerView

enum class HelpRequestRowStyle {
    /** Patient help list: show responder as title. */
    PATIENT,

    /** Responder queue: show patient as title. */
    RESPONDER,

    /** Admin overview: patient + responder context. */
    ADMIN
}

class HelpRequestAdapter(
    private val rowStyle: HelpRequestRowStyle,
    private val onClick: (HelpRequest) -> Unit
) : ListAdapter<HelpRequest, HelpRequestAdapter.VH>(DIFF) {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_help_request, parent, false)
        return VH(v)
    }

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(getItem(position), rowStyle, onClick)
    }

    class VH(itemView: View) : RecyclerView.ViewHolder(itemView) {
        private val tvTitle: TextView = itemView.findViewById(R.id.tvHelpTitle)
        private val tvSubtitle: TextView = itemView.findViewById(R.id.tvHelpSubtitle)
        private val tvStatus: TextView = itemView.findViewById(R.id.tvHelpStatus)

        fun bind(item: HelpRequest, rowStyle: HelpRequestRowStyle, onClick: (HelpRequest) -> Unit) {
            val ctx = itemView.context
            when (rowStyle) {
                HelpRequestRowStyle.PATIENT -> {
                    tvTitle.text = ctx.getString(R.string.help_row_responder_format, item.responderName.ifBlank { "—" })
                    tvSubtitle.text = item.locationSummary.ifBlank { ctx.getString(R.string.help_row_no_location) }
                }
                HelpRequestRowStyle.RESPONDER -> {
                    tvTitle.text = item.patientName.ifBlank { ctx.getString(R.string.dashboard_name_fallback) }
                    tvSubtitle.text = item.locationSummary.ifBlank { ctx.getString(R.string.help_row_no_location) }
                }
                HelpRequestRowStyle.ADMIN -> {
                    tvTitle.text = item.patientName.ifBlank { ctx.getString(R.string.dashboard_name_fallback) }
                    tvSubtitle.text = ctx.getString(
                        R.string.admin_help_row_subtitle,
                        item.responderName.ifBlank { "—" },
                        item.locationSummary.ifBlank { ctx.getString(R.string.help_row_no_location) }
                    )
                }
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
