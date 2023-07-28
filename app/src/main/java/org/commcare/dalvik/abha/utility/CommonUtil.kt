package org.commcare.dalvik.abha.utility

import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

object CommonUtil {

    fun getFormattedDateTime(ts: Long,dateFormat:String): String? {
        return try {
            val sdf = SimpleDateFormat(dateFormat , Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val netDate = Date(ts)
            sdf.format(netDate)
        } catch (e: Exception) {
            null
        }
    }
}