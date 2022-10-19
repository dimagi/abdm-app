package org.commcare.dalvik.abha.model

import androidx.databinding.BaseObservable
import androidx.databinding.Bindable
import org.commcare.dalvik.abha.BR

class AbhaVerificationRequestModel(val abhaId: String) : BaseObservable() {

    var txnId: String = ""
        @Bindable get
        set(value) {
            field = value
            notifyPropertyChanged(BR.txnId)
        }

}