package com.example.pulse

/** Shared demo ledger used by Transparency, Transactions, and Revenue screens. */
object FinanceSampleData {
    val transactions: List<Transaction> = listOf(
        Transaction("1", "Donation: North Cebu Relief Fund", "May 01", 15000.00, "INCOME"),
        Transaction("2", "Purchased: First Aid Kit Batch A", "May 03", 2450.00, "EXPENSE"),
        Transaction("3", "Anonymous Donation", "May 05", 500.00, "INCOME"),
        Transaction("4", "Medical Supplies: Oxygen Tank", "May 07", 8500.00, "EXPENSE"),
        Transaction("5", "Community Health Fund Grant", "May 08", 20000.00, "INCOME")
    )

    fun netBalance(): Double =
        transactions.sumOf { if (it.type == "INCOME") it.amount else -it.amount }

    fun revenueTotal(): Double =
        transactions.filter { it.type == "INCOME" }.sumOf { it.amount }
}
