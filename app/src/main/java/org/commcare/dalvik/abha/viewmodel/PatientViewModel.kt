package org.commcare.dalvik.abha.viewmodel

import dagger.hilt.android.lifecycle.HiltViewModel
import org.commcare.dalvik.abha.model.ConsentPermission
import org.commcare.dalvik.abha.model.Hiu
import org.commcare.dalvik.abha.model.Patient
import org.commcare.dalvik.abha.model.PatientConsentModel
import org.commcare.dalvik.abha.model.Purpose
import org.commcare.dalvik.abha.model.Requester
import org.commcare.dalvik.abha.ui.main.fragment.PatientConsentFragment
import javax.inject.Inject

@HiltViewModel
class PatientViewModel @Inject constructor() : BaseViewModel() {
    lateinit var patientConsentModel: PatientConsentModel

    fun init() {
        this.patientConsentModel =
            PatientConsentModel(Purpose(PatientConsentFragment.PURPOSE.CAREMGT)).apply {
                hiu = Hiu("abc@test.com")
                patient = Patient("hinapatel@ndhm")
                requester = Requester("Dr. Manju")
                permission = ConsentPermission(PatientConsentFragment.ACCESS_MODE.VIEW.value)
            }

    }

}
