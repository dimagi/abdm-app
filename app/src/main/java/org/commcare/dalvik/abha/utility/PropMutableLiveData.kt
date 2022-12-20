package org.commcare.dalvik.abha.utility

import androidx.databinding.BaseObservable
import androidx.databinding.Observable
import androidx.lifecycle.MutableLiveData

class PropMutableLiveData<T:BaseObservable>: MutableLiveData<T> (){

    override fun setValue(value: T) {
        super.setValue(value)
        value.addOnPropertyChangedCallback(propChangeCallback)
    }

    val propChangeCallback = object: Observable.OnPropertyChangedCallback() {
        override fun onPropertyChanged(sender: Observable?, propertyId: Int) {
            value?.let { setValue(it) }
        }
    }

}