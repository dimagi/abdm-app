package org.commcare.dalvik.domain.model

import com.google.gson.annotations.SerializedName

data class ConsentArtefactModel(
    val id: Int,

    @SerializedName("artefact_id")
    val artefactId: String,

    @SerializedName("date_created")
    val creationDate: String?,

    @SerializedName("last_modified")
    val lastModified: String?,

    @SerializedName("fetch_status")
    val status: String,

    val error: String?,

    @SerializedName("consent_request")
    val consentRequest: String,

    val details: ConsentArtefactDetailModel?

){
    fun isArtefactRequested() = status == "REQUESTED"
}

