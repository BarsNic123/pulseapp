package com.example.pulse

object ResponderManager {
    val list = mutableListOf(
        Responder(
            name = "Jehan Ishi",
            age = "22",
            status = "ONLINE",
            expertise = listOf("Medical Transport", "First Aid"),
            bio = "Emergency responder based in Mandaue."
        ),
        Responder(
            name = "Nicole Baring",
            age = "22",
            status = "OFFLINE",
            expertise = listOf("First Aid"),
            bio = "Available for weekend shifts."
        ),
        Responder(
            name = "Dane Antonie",
            age = "23",
            status = "ONLINE",
            expertise = listOf("Elderly Companion"),
            bio = "Certified caregiver."
        )
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