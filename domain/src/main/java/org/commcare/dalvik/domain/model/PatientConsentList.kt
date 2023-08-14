package org.commcare.dalvik.domain.model

data class PatientConsentList(
    val count: Int,
    val next: String,
    val previous: String,
    val results: List<PatientConsentModel>
)
