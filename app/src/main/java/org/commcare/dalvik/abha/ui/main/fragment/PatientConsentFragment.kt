package org.commcare.dalvik.abha.ui.main.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import org.commcare.dalvik.abha.databinding.PatientConsentBinding
import org.commcare.dalvik.abha.ui.main.adapters.PatientConsentAdapter
import org.commcare.dalvik.abha.viewmodel.PatientViewModel

class PatientConsentFragment : BaseFragment<PatientConsentBinding>(PatientConsentBinding::inflate) {

    val viewModel: PatientViewModel by activityViewModels()
    lateinit var consentAdapter: PatientConsentAdapter
    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        consentAdapter = PatientConsentAdapter()
        binding.consentList.setHasFixedSize(true)
        binding.consentList.adapter = consentAdapter


        viewModel.fetchPatientConsent().observe(viewLifecycleOwner) {
            consentAdapter.submitData(lifecycle, it)

        }


    }

}