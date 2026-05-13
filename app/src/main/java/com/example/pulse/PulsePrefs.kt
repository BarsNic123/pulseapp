package com.example.pulse

import android.content.Context

object PulsePrefs {
    private const val PREFS = "pulse_prefs"
    private const val KEY_LAST_VOICE_URL = "last_voice_download_url"

    fun saveLastVoiceUrl(context: Context, url: String?) {
        val e = context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).edit()
        if (url.isNullOrBlank()) e.remove(KEY_LAST_VOICE_URL) else e.putString(KEY_LAST_VOICE_URL, url)
        e.apply()
    }

    fun getLastVoiceUrl(context: Context): String? {
        return context.getSharedPreferences(PREFS, Context.MODE_PRIVATE).getString(KEY_LAST_VOICE_URL, null)
            ?.takeIf { it.isNotBlank() }
    }
}
