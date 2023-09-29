package org.commcare.dalvik.abha.utility

import org.commcare.dalvik.abha.R

class AppConstants {
    companion object{
        val OTP_LENGTH = 6
        val MOBILE_OTP_LENGTH = 6
        val AADHAAR_OTP_LENGTH = 6
        val AADHAR_NUMBER_LENGTH = 12
        val MOBILE_NUMBER_LENGTH = 10
        val OTP_BLOCK_TS = 30 * 60 * 1000

        val abhaHealthLangKeysMap:MutableMap<String,Int> = mutableMapOf<String, Int>().apply {
            put("name",R.string.name)
            put("gender",R.string.gender)
            put("yearOfBirth",R.string.yearOfBirth)
            put("monthOfBirth",R.string.monthOfBirth)
            put("dayOfBirth",R.string.dayOfBirth)
            put("firstName",R.string.firstName)
            put("healthId",R.string.healthId)
            put("lastName",R.string.lastName)
            put("middleName",R.string.middleName)
            put("stateCode",R.string.stateCode)
            put("districtCode",R.string.districtCode)
            put("stateName",R.string.stateName)
            put("districtName",R.string.districtName)
            put("email",R.string.email)
            put("kycPhoto",R.string.kycPhoto)
            put("profilePhoto",R.string.profilePhoto)
            put("mobile",R.string.mobile)
            put("pincode",R.string.pincode)
            put("authMethods",R.string.authMethods)
            put("healthIdNumber",R.string.healthIdNumber)
        }

    }
}