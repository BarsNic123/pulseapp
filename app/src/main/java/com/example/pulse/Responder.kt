package com.example.pulse

data class Responder(
    val uid: String? = null,
    val name: String,
    val age: String,
    val status: String,
    val expertise: List<String>,
    val bio: String
)