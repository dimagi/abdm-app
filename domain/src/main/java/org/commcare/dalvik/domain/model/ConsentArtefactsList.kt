package org.commcare.dalvik.domain.model

data class ConsentArtefactsList(
    val count: Int,
    val next: String?,
    val previous: String?,
    var results: List<ConsentArtefactModel>
)
