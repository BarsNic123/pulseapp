package com.example.pulse

data class Transaction(
    val id: String,
    val title: String,
    val date: String,
    val amount: Double,
    val type: String // Use "INCOME" or "EXPENSE"
)