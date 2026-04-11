package com.example.pulse

object ResponderManager {
    val list = mutableListOf<Responder>(
        Responder("Jehan Ishi", "22", "ONLINE", listOf("Medical Transport", "First Aid"), "Emergency responder based in Mandaue."),
        Responder("Nicole Baring", "22", "OFFLINE", listOf("First Aid"), "Available for weekend shifts."),
        Responder("Dane Antonie", "23", "ONLINE", listOf("Elderly Companion"), "Certified caregiver.")
    )

    fun updateOrAdd(oldName: String?, newResponder: Responder) {
        val index = list.indexOfFirst { it.name == oldName }
        if (index != -1) {
            list[index] = newResponder
        } else {
            list.add(newResponder)
        }
    }
}