package com.example.pulse

object PulseRoles {
    const val ROLE_ADMIN = "Admin"
    const val ROLE_PATIENT = "Patient"
    const val ROLE_RESPONDER = "Responder"
    const val ROLE_RESPONDENT = "Respondent"

    /** Firebase Auth account used for administrator access. */
    const val ADMIN_EMAIL = "admin@pulse.com"

    fun isAdminEmail(email: String?): Boolean =
        email?.equals(ADMIN_EMAIL, ignoreCase = true) == true
}
