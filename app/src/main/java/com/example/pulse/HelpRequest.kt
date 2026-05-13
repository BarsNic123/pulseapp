package com.example.pulse

import com.google.firebase.firestore.DocumentSnapshot

object HelpRequestStatus {
    const val PENDING = "pending"
    const val ACCEPTED = "accepted"
    const val DONE = "done"
}

data class HelpRequest(
    val id: String,
    val patientUid: String,
    val patientName: String,
    val patientAge: String,
    val patientBloodType: String,
    val patientEmergency: String,
    val patientRiskLevel: String,
    val patientMedicalNotes: String,
    val responderUid: String,
    val responderName: String,
    val status: String,
    val locationSummary: String,
    val voiceUrl: String?,
    val createdAtMillis: Long,
    val patientSeenCompletion: Boolean
) {
    fun statusLabel(): String = when (status) {
        HelpRequestStatus.PENDING -> "Pending"
        HelpRequestStatus.ACCEPTED -> "In progress"
        HelpRequestStatus.DONE -> "Completed"
        else -> status
    }

    companion object {
        fun from(doc: DocumentSnapshot): HelpRequest? {
            val patientUid = doc.getString("patientUid") ?: return null
            val responderUid = doc.getString("responderUid") ?: return null
            val createdAt = doc.getTimestamp("createdAt")?.toDate()?.time ?: 0L
            return HelpRequest(
                id = doc.id,
                patientUid = patientUid,
                patientName = doc.getString("patientName").orEmpty(),
                patientAge = doc.getString("patientAge").orEmpty(),
                patientBloodType = doc.getString("patientBloodType").orEmpty(),
                patientEmergency = doc.getString("patientEmergency").orEmpty(),
                patientRiskLevel = doc.getString("patientRiskLevel").orEmpty(),
                patientMedicalNotes = doc.getString("patientMedicalNotes").orEmpty(),
                responderUid = responderUid,
                responderName = doc.getString("responderName").orEmpty(),
                status = doc.getString("status") ?: HelpRequestStatus.PENDING,
                locationSummary = doc.getString("locationSummary").orEmpty(),
                voiceUrl = doc.getString("voiceUrl"),
                createdAtMillis = createdAt,
                patientSeenCompletion = when {
                    !doc.contains("patientSeenCompletion") -> true
                    else -> doc.getBoolean("patientSeenCompletion") == true
                }
            )
        }
    }
}
