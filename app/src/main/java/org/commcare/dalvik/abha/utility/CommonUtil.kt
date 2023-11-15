package org.commcare.dalvik.abha.utility

import org.commcare.dalvik.domain.model.DATE_FORMAT
import timber.log.Timber
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

    fun getUtcTimeFromDate(date: String, dateFormat: DATE_FORMAT = DATE_FORMAT.SERVER): String? {
        try {
            Timber.d("Server Date --> ${date}")
            val sdf = SimpleDateFormat(DATE_FORMAT.SERVER.format, Locale.getDefault())
            sdf.timeZone = TimeZone.getTimeZone("UTC")
            var date = sdf.parse(date)

            Timber.d("UTC   Date --> ${date}")

            val ndf = SimpleDateFormat(dateFormat.format, Locale.getDefault())
            val newDate = ndf.format(date)
            Timber.d("FORMATTED UTC   Date --> ${newDate}")
            return newDate
        } catch (e: Exception) {
            Timber.d("EXCEPTION --> ${e.message}")
            return ""
        }
    }
}