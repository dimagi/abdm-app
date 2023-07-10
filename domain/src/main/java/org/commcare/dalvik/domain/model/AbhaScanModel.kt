package org.commcare.dalvik.domain.model

import com.google.gson.annotations.SerializedName

data class AbhaScanModel(
    val hidn:String?,
    val hid:String?,
    val name:String?,
    val gender:String?,
    val statelgd:String?,
    val distlgd:String?,
    val dob:String?,
    @SerializedName("state name")
    val stateName:String?,
    val district_name:String?,
    val mobile:String?,
    val address:String?
) {
    fun getAbha(): String? {
        hidn?.let {
            return it.replace("-", "")
        }?: run {
            return null
        }
    }
}