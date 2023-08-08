package org.commcare.dalvik.domain.model

import com.google.gson.annotations.SerializedName
import java.text.SimpleDateFormat
import java.util.Date

data class PatientConsentDetailModel(
    val purpose: Purpose
) {
    lateinit var patient: Patient
    lateinit var hiu: Hiu
    lateinit var requester: Requester
    var hiTypes = mutableListOf<String>()
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

    fun setPermissionStartDate(startDate: String) {
        permission.dateRange.startDate = startDate
    }

    fun setPermissionEndDate(endDate: String) {
        permission.dateRange.endDate = endDate
    }

    fun setPermissionExpiryDate(removalDate: String) {
        permission.expiryDate = removalDate
    }

    fun getPermissionStartDate() =
        permission.dateRange.startDate

    fun getPermissionEndDate() =
        permission.dateRange.endDate


    fun getPermissionStartDateInMs():Long{
        var date:Date
        val formatter = SimpleDateFormat(DATE_FORMAT.SERVER.format)
        date = formatter.parse(permission.dateRange.startDate)
        return date.time
    }

    fun getPermissionEndDateInMs():Long{
        var date:Date
        val formatter = SimpleDateFormat(DATE_FORMAT.SERVER.format)
        date = formatter.parse(permission.dateRange.endDate)
        return date.time
    }

}

data class Purpose(val code:String ){
    lateinit var text:String
    lateinit var refUri:String

}
data class Patient(val id: String)

data class Hiu(val id: String)
data class Requester(val name: String, val identifier: Identifier = Identifier())

data class Identifier(
    val type: String = "REGNO",
    val value: String = "MH1001",
    val system: String = "https://www.mciindia.org"
)

data class DateRange(@SerializedName("from") var startDate: String? = null ,@SerializedName("to")var endDate: String?=null ) {

    fun validate(): ConsentValidation {

        if (startDate == null) {
            return ConsentValidation.INVALID_START_DATE
        }
        if (endDate == null) {
            return ConsentValidation.INVALID_END_DATE
        }

        return ConsentValidation.SUCCESS

    }

//    fun getJson() = JSONObject().apply {
//        put(
//            "from", CommonUtil.getFormattedDateTime(
//                startDate,
//                PatientConsentFragment.DATE_FORMAT.SERVER.format
//            )
//        )
//
//        put(
//            "to", CommonUtil.getFormattedDateTime(
//                endDate,
//                PatientConsentFragment.DATE_FORMAT.SERVER.format
//            )
//        )
//    }
}

data class Frequency(val unit: String = "HOUR", val value: Int = 0, val repeats: Int = 0)

data class ConsentPermission(val accessMode: String) {
    @SerializedName("dataEraseAt")
    lateinit var expiryDate: String
    var frequency: Frequency = Frequency()
    val dateRange: DateRange = DateRange()

    fun validate(): ConsentValidation {
        val dateRangeValidation = dateRange.validate()

        if(dateRangeValidation != ConsentValidation.SUCCESS){
            return dateRangeValidation
        }
        if (expiryDate == null) {
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

enum class DATE_FORMAT(val format: String) {
    SERVER("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
    USER("dd MMM YYYY , hh:mm a")
}





