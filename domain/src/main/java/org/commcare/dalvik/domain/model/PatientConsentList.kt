package org.commcare.dalvik.domain.model

data class PatientConsentList(
    val count: Int,
    val next: String,
    val previous: Any,
    val results: List<PatientConsentModel>
)
