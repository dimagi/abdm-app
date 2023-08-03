package org.commcare.dalvik.abha.model

import com.google.gson.Gson
import org.commcare.dalvik.abha.ui.main.fragment.PatientConsentFragment
import org.commcare.dalvik.abha.utility.CommonUtil
import org.json.JSONArray
import org.json.JSONObject

data class PatientConsentModel(
    val consentPurpose: Purpose
) {
    lateinit var patient: Patient
    lateinit var hiu: Hiu
    lateinit var requester: Requester
    var hiTypes = mutableListOf<PatientConsentFragment.HITYPES>()
    lateinit var permission: ConsentPermission

    fun validateConsent(): ConsentValidation {

        if (hiTypes.isEmpty()) {
            return ConsentValidation.INVALID_HI_TYPE
        }

        if (permission == null) {
            return ConsentValidation.INVALID_HI_TYPE
        }

        val permissonValidation = permission.validate()

        if (permissonValidation != ConsentValidation.SUCCESS) {
            return permissonValidation
        }



        return ConsentValidation.SUCCESS

    }

    fun setPermissionStartDate(startDate: Long) {
        permission.dateRange.startDate = startDate
    }

    fun setPermissionEndDate(endDate: Long) {
        permission.dateRange.endDate = endDate
    }

    fun setPermissionExpiryDate(removalDate: Long) {
        permission.expiryDate = removalDate
    }

    fun getPermissionStartDate() =
        permission.dateRange.startDate

    fun getPermissionEndDate() =
        permission.dateRange.endDate


    fun getConsentJsonData(): JSONObject {
        val jsonData = JSONObject().apply {
            //PURPOSE
            val purposeJson = JSONObject()
            purposeJson.put("code", consentPurpose.purpose.name)
            put("purpose", purposeJson)

            //PATIENT
            put("patient", Gson().toJson(patient))

            //HIU
            put("hiu", Gson().toJson(hiu))

            //HI TYPES
            val hiTypesArr = JSONArray()
            hiTypes.forEach {
                hiTypesArr.put(it.displayValue)
            }

            put("hiTypes", hiTypesArr)

            //PERMISSION
            val permissionJson = JSONObject().apply {
                put("accessMode", permission.accessMode)
                put(
                    "dataEraseAt", CommonUtil.getFormattedDateTime(
                        permission.expiryDate,
                        PatientConsentFragment.DATE_FORMAT.SERVER.format
                    )
                )

                put("frequency", Gson().toJson(permission.frequency))

                put("dateRange", permission.dateRange.getJson())
            }

            put("permission", permissionJson)

            //REQUESTER
            put("requester", Gson().toJson(requester))

        }

        return jsonData
    }

}

data class Purpose(val purpose: PatientConsentFragment.PURPOSE)
data class Patient(val id: String)

data class Hiu(val id: String)
data class Requester(val name: String, val identifier: Identifier = Identifier())

data class Identifier(
    val type: String = "REGNO",
    val value: String = "MH1001",
    val system: String = "https://www.mciindia.org"
)

data class DateRange(var startDate: Long = 0L, var endDate: Long = 0L) {

    fun validate(): ConsentValidation {

        if (startDate == 0L) {
            return ConsentValidation.INVALID_START_DATE
        }
        if (endDate == 0L) {
            return ConsentValidation.INVALID_END_DATE
        }
        if (endDate <= startDate) {
            return ConsentValidation.INVALID_START_END_DATE_RANGE
        }

        return ConsentValidation.SUCCESS

    }

    fun getJson() = JSONObject().apply {
        put(
            "from", CommonUtil.getFormattedDateTime(
                startDate,
                PatientConsentFragment.DATE_FORMAT.SERVER.format
            )
        )

        put(
            "to", CommonUtil.getFormattedDateTime(
                endDate,
                PatientConsentFragment.DATE_FORMAT.SERVER.format
            )
        )
    }
}

data class Frequency(val unit: String = "HOUR", val value: Int = 0, val repeats: Int = 0)

data class ConsentPermission(val accessMode: String) {
    var expiryDate: Long = 0L
    var frequency: Frequency = Frequency()
    val dateRange: DateRange = DateRange()

    fun validate(): ConsentValidation {
        val dateRangeValidation = dateRange.validate()

        if(dateRangeValidation != ConsentValidation.SUCCESS){
            return dateRangeValidation
        }
        if (expiryDate == 0L) {
            return ConsentValidation.INVALID_EXPIRY_DATE
        }
        return ConsentValidation.SUCCESS
    }
}

enum class ConsentValidation(val msg: String) {
    SUCCESS("Valid consent"),
    INVALID_START_DATE("Invalid start date."),
    INVALID_END_DATE("Invalid end date."),
    INVALID_EXPIRY_DATE("Invalid expiry date."),
    INVALID_START_END_DATE_RANGE("Invalid start-end date range."),
    INVALID_HI_TYPE("HI_TYPE not selected")
}


