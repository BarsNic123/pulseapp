package com.example.pulse

import android.media.MediaPlayer
import android.view.LayoutInflater
import android.view.ViewGroup
import android.widget.Toast
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.button.MaterialButton
import java.text.DateFormat
import java.util.Date

data class VoiceNoteRow(
    val id: String,
    val title: String,
    val downloadUrl: String,
    val createdAtMillis: Long
)

class VoiceNotesAdapter(
    private val onPlayError: (String) -> Unit
) : RecyclerView.Adapter<VoiceNotesAdapter.VH>() {

    private val items = mutableListOf<VoiceNoteRow>()
    private var playingId: String? = null
    private var player: MediaPlayer? = null

    fun submit(list: List<VoiceNoteRow>) {
        items.clear()
        items.addAll(list)
        notifyDataSetChanged()
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): VH {
        val v = LayoutInflater.from(parent.context).inflate(R.layout.item_voice_note, parent, false)
        return VH(v)
    }

    override fun getItemCount() = items.size

    override fun onBindViewHolder(holder: VH, position: Int) {
        holder.bind(items[position])
    }

    inner class VH(private val root: android.view.View) : RecyclerView.ViewHolder(root) {
        private val title = root.findViewById<android.widget.TextView>(R.id.tvVoiceNoteTitle)
        private val time = root.findViewById<android.widget.TextView>(R.id.tvVoiceNoteTime)
        private val play = root.findViewById<MaterialButton>(R.id.btnPlayRow)

        fun bind(row: VoiceNoteRow) {
            title.text = row.title
            time.text = DateFormat.getDateTimeInstance().format(Date(row.createdAtMillis))
            play.text = root.context.getString(
                if (playingId == row.id) R.string.case_stop_voice else R.string.case_play_voice
            )
            play.setOnClickListener {
                if (playingId == row.id && player?.isPlaying == true) {
                    stop()
                    play.text = root.context.getString(R.string.case_play_voice)
                    notifyDataSetChanged()
                    return@setOnClickListener
                }
                stop()
                notifyDataSetChanged()
                try {
                    player = MediaPlayer().apply {
                        setDataSource(row.downloadUrl)
                        setOnCompletionListener {
                            playingId = null
                            play.text = root.context.getString(R.string.case_play_voice)
                            stop()
                            notifyDataSetChanged()
                        }
                        prepare()
                        start()
                    }
                    playingId = row.id
                    notifyDataSetChanged()
                } catch (e: Exception) {
                    onPlayError(e.message ?: "")
                }
            }
        }
    }

    fun stop() {
        try {
            player?.release()
        } catch (_: Exception) {
        }
        player = null
        playingId = null
    }

    override fun onDetachedFromRecyclerView(recyclerView: RecyclerView) {
        super.onDetachedFromRecyclerView(recyclerView)
        stop()
    }
}
