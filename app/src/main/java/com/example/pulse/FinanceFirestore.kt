package com.example.pulse

import com.google.firebase.Timestamp
import com.google.firebase.auth.FirebaseAuth
import com.google.firebase.firestore.DocumentSnapshot
import com.google.firebase.firestore.FirebaseFirestore
import com.google.firebase.firestore.ListenerRegistration
import com.google.firebase.firestore.Query
import java.util.Date

object FinanceFirestore {
    const val COLLECTION = "finance_transactions"

    fun docToTransaction(doc: DocumentSnapshot): Transaction? {
        val title = doc.getString("title") ?: return null
        val type = doc.getString("type") ?: return null
        val amount = doc.getDouble("amount") ?: return null
        val dateLabel = doc.getString("dateLabel").orEmpty()
        return Transaction(doc.id, title, dateLabel, amount, type)
    }

    fun listen(db: FirebaseFirestore, onUpdate: (List<Transaction>) -> Unit): ListenerRegistration {
        return db.collection(COLLECTION)
            .orderBy("createdAt", Query.Direction.DESCENDING)
            .addSnapshotListener { snap, e ->
                if (e != null || snap == null) {
                    onUpdate(emptyList())
                    return@addSnapshotListener
                }
                onUpdate(snap.documents.mapNotNull { docToTransaction(it) })
            }
    }

    fun netBalance(list: List<Transaction>): Double =
        list.sumOf { if (it.type == "INCOME") it.amount else -it.amount }

    fun revenueTotal(list: List<Transaction>): Double =
        list.filter { it.type == "INCOME" }.sumOf { it.amount }

    /** One-time seed when collection is empty (admin only, via security rules). */
    fun seedDefaultsIfEmpty(db: FirebaseFirestore, auth: FirebaseAuth, onComplete: () -> Unit) {
        val user = auth.currentUser ?: run {
            onComplete()
            return
        }
        if (!PulseRoles.isAdminEmail(user.email)) {
            onComplete()
            return
        }
        db.collection(COLLECTION).limit(1).get()
            .addOnSuccessListener { snap ->
                if (!snap.isEmpty) {
                    onComplete()
                    return@addOnSuccessListener
                }
                val batch = db.batch()
                var offset = 0L
                for (t in FinanceSampleData.transactions) {
                    val ref = db.collection(COLLECTION).document()
                    batch.set(
                        ref,
                        hashMapOf(
                            "title" to t.title,
                            "type" to t.type,
                            "amount" to t.amount,
                            "dateLabel" to t.date,
                            "createdAt" to Timestamp(Date(System.currentTimeMillis() + offset))
                        )
                    )
                    offset += 60_000L
                }
                batch.commit().addOnCompleteListener { onComplete() }
            }
            .addOnFailureListener { onComplete() }
    }
}
