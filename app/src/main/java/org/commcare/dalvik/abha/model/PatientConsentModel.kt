package org.commcare.dalvik.abha.model

data class PatientConsentModel(
    val purpose: Purpose
) {
    lateinit var patient: Patient
    lateinit var hip: Hip
    lateinit var hiu: Hiu
    lateinit var requester: Requester
    lateinit var hiTypes: MutableList<String>
    lateinit var permission: ConsentPermission

    fun setPermissionStartDate(startDate:Long){
        permission.dateRange.startDate = startDate
    }

    fun setPermissionEndDate(endDate:Long){
        permission.dateRange.endDate = endDate
    }

    fun setPermissionExpiryDate(removalDate:Long){
        permission.expiryDate = removalDate
    }

    fun getPermissionStartDate() =
         permission.dateRange.startDate

    fun getPermissionEndDate() =
        permission.dateRange.endDate

}

data class Purpose(val code: String)
data class Patient(val id: String)
data class Hip(val id: String)
data class Hiu(val id: String)
data class Requester(val name: String)

data class DateRange(var startDate: Long = 0L, var endDate: Long = 0L)

data class Frequency(val unit: String, val value: Int = 0, val repeat: Int = 0)

data class ConsentPermission(val accessMode: String) {
    var expiryDate: Long = 0L
    lateinit var frequency: Frequency
    val dateRange: DateRange = DateRange()
}


