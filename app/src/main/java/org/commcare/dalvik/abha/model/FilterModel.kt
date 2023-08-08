package org.commcare.dalvik.abha.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import org.commcare.dalvik.abha.BR

class FilterModel: BaseObservable() {

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

    fun clear(){
        filterText = null
        fromDate = null
        toDate = null
    }

    fun setFilterStartDate(date:String?){
        fromDate = date
    }

    fun setFilterEndDate(date:String?){
        toDate = date
    }
}