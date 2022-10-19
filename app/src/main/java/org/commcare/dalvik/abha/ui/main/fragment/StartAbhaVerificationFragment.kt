package org.commcare.dalvik.abha.ui.main.fragment

import android.os.Bundle
import android.os.Handler
import android.os.Looper
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import kotlinx.coroutines.launch
import org.commcare.dalvik.abha.R
import org.commcare.dalvik.abha.databinding.StartAbhaVerificationBinding
import org.commcare.dalvik.abha.model.AbhaRequestModel
import org.commcare.dalvik.abha.viewmodel.GenerateAbhaUiState
import org.commcare.dalvik.abha.viewmodel.AbdmViewModel
import org.commcare.dalvik.abha.viewmodel.OtpCallState

class StartAbhaVerificationFragment():BaseFragment<StartAbhaVerificationBinding>(StartAbhaVerificationBinding::inflate) {

    private val viewModel: AbdmViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.clickHandler = this
        populateIntentData()
        attachUiStateObserver()
    }

    private fun populateIntentData() {
        arguments?.getString("abha_id")?.apply {
            binding.abhaNumberEt.setText(this)
            val abhaRequestModel = AbhaRequestModel()
            abhaRequestModel.abhaId = this
            viewModel.init(abhaRequestModel)
        }

    }

    override fun onClick(view: View?) {
        super.onClick(view)
        lifecycleScope.launch{
            viewModel.abhaRequestModel.value?.abhaId?.let { abhaIdKey ->
                viewModel.checkForBlockedState(abhaIdKey).collect {
                    when (it) {
                        OtpCallState.OtpReqAvailable -> {
                            findNavController().navigate(R.id.action_startAbhaVerificationFragment_to_selectAuthenticationFragment ,arguments)
                        }
                        is OtpCallState.OtpReqBlocked -> {
                            viewModel.otpRequestBlocked.value = it.otpRequestCallModel
                        }
                    }
                }
            }
        }
    }

    private fun attachUiStateObserver() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    when (it) {
                        is GenerateAbhaUiState.TranslationReceived ->{
                            Handler(Looper.getMainLooper()).post {
                                binding.invalidateAll()
                            }

                        }
                    }
                }
            }
        }
    }
}