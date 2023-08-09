package org.commcare.dalvik.abha.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import org.commcare.dalvik.abha.BR
import org.commcare.dalvik.abha.utility.CommonUtil
import org.commcare.dalvik.domain.model.DATE_FORMAT
import java.text.SimpleDateFormat
import java.util.Date
import java.util.Locale
import java.util.TimeZone

class FilterModel : BaseObservable() {

    var startDateFilterInMS:Long? = null

    var filterText: String? = null
        @Bindable get
        set(value) {
            field = value
            notifyPropertyChanged(BR.filterText)
        }

    var fromDate: String? = null
        @Bindable get
        set(value) {
            field = value
            notifyPropertyChanged(BR.fromDate)
        }

    var toDate: String? = null
        @Bindable get
        set(value) {
            field = value
            notifyPropertyChanged(BR.toDate)
        }

    fun clear() {
        filterText = null
        fromDate = null
        toDate = null
        startDateFilterInMS = null
    }

    fun setFilterStartDate(date: String?) {
        fromDate = date
    }

    fun setFilterEndDate(date: String?) {
        toDate = date
    }
}