package com.example.pulse

/**
 * Cebu City only — hospitals and universities for affiliation picker during registration.
 */
object CebuInstitutions {

    const val TYPE_HOSPITAL = "Hospital"
    const val TYPE_UNIVERSITY = "University"

    val hospitalsCebuCity = listOf(
        "ACE Medical Center – Cebu",
        "Cebu City Medical Center",
        "Cebu Doctors’ University Hospital",
        "Cebu Velez General Hospital",
        "Chong Hua Hospital",
        "Perpetual Succour Hospital",
        "UCMed – University of Cebu Medical Center",
        "Visayas Community Medical Center"
    ).sorted()

    val universitiesCebuCity = listOf(
        "University of San Carlos",
        "University of Cebu",
        "Cebu Normal University",
        "Cebu Technological University – Main",
        "Southwestern University PHINMA",
        "University of Southern Philippines Foundation",
        "University of the Visayas",
        "Cebu Institute of Technology – University",
        "Saint Theresa's College of Cebu",
        "Asian College of Technology",
        "Salazar Colleges of Science and Institute of Technology"
    ).sorted()
}
