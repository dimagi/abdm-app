package org.commcare.dalvik.abha.utility

import org.commcare.dalvik.domain.model.DATE_FORMAT
import java.text.SimpleDateFormat
import java.util.*

object CommonUtil {

    fun getTimeInMillis(date: String): Long {
        val sdf = SimpleDateFormat(DATE_FORMAT.SERVER.format)
        val date = sdf.parse(date)
        return date.time
    }

    fun getUserFormatDate(serverDate: String): String? {
        var date: Date
        val formatter = SimpleDateFormat(DATE_FORMAT.SERVER.format)
        date = formatter.parse(serverDate)
        return getFormattedDateTime(date.time, DATE_FORMAT.USER.format)
    }

    fun getFormattedDateTime(ts: Long, dateFormat: String): String? {
        return try {
            val sdf = SimpleDateFormat(dateFormat, Locale.US)
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            val netDate = Date(ts)
            sdf.format(netDate)
        } catch (e: Exception) {
            null
        }
    }

    fun getUtcTimeFromDate(date: String): String? {
        val UTC_DIFF = (30 * 60 * 1000) + (5 * 60 * 60 * 1000)
        val finalRecordDate = getTimeInMillis(date) - UTC_DIFF
        return getFormattedDateTime(finalRecordDate , DATE_FORMAT.SERVER.format)
    }
}