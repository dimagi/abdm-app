package org.commcare.dalvik.abha.model

import com.google.gson.annotations.SerializedName

data class AbhaScanModel(
    val hidn: String?,
    var hid: String?,
    val name: String?,
    val gender: String?,
    val statelgd: String?,
    val distlgd: String?,
    val dob: String?,
    @SerializedName("state name")
    val stateName: String?,
    val district_name: String?,
    val mobile: String?,
    val address: String?,
    var validationState: AbhaValidationState = AbhaValidationState.NOT_VALIDATED
    )   {

    fun getAbha(): String? {
        hid?.let {
            return it
        }
        hidn?.let {
            return it.replace("-", "")
        } ?: run {
            return null
        }
    }

    enum class AbhaValidationState(val value: String) {
        VALID("VALID"),
        INVALID("INVALID"),
        NOT_VALIDATED("NOT VALIDATED");
    }
}