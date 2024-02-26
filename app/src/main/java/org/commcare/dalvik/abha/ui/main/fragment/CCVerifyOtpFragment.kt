package org.commcare.dalvik.abha.ui.main.fragment

import android.os.Bundle
import android.view.View
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.commcare.dalvik.abha.R
import org.commcare.dalvik.abha.databinding.GenerateCCOtpBinding
import org.commcare.dalvik.abha.ui.main.activity.AbdmActivity
import org.commcare.dalvik.abha.ui.main.activity.VerificationMode
import org.commcare.dalvik.abha.ui.main.custom.OtpTimerState
import org.commcare.dalvik.abha.utility.AppConstants
import org.commcare.dalvik.abha.utility.observeText
import org.commcare.dalvik.abha.viewmodel.CareContextViewModel
import org.commcare.dalvik.abha.viewmodel.GenerateAbhaUiState
import org.commcare.dalvik.abha.viewmodel.OtpCallState
import org.commcare.dalvik.abha.viewmodel.RequestType
import org.commcare.dalvik.domain.model.CCGenerateOtpResponseModel
import org.commcare.dalvik.domain.model.CCVerifyOtpResponseModel
import org.commcare.dalvik.domain.model.ConfirmAuthModel
import org.commcare.dalvik.domain.model.Credential
import org.commcare.dalvik.domain.model.Demographic

class CCVerifyOtpFragment : BaseFragment<GenerateCCOtpBinding>(GenerateCCOtpBinding::inflate) {

    val viewModel: CareContextViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.clickHandler = this

        arguments?.let {
            it.getBoolean("verifyWithDemographics").let {
                binding.isVerifyWithDemoGraphics = it
                if(it) {
                    (activity as AbdmActivity).setToolbarTitle(R.string.demographics)
                }
            }

            it.getSerializable("demographic")?.let { model ->
                binding.demograhicModel = model as Demographic
            }

        }

        lifecycleScope.launch(Dispatchers.Main) {
            binding.otpEt.observeText().collect {
                binding.verifyCCOtp.isEnabled = it == AppConstants.OTP_LENGTH
            }
        }

        observeUiState()
        observeOtpTimer()
        requestOtp()
    }

    private fun observeOtpTimer() {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.timeProgress.timeState.collect {
                when (it) {
                    OtpTimerState.None -> {
                        // Nothing for now
                    }

                    OtpTimerState.TimerStarted -> {
                        binding.resendCCOtp.isEnabled = false
                    }

                    OtpTimerState.TimerOver -> {
                        binding.resendCCOtp.isEnabled = true
//                        viewModel.getData(PrefKeys.OTP_BLOCKED_TS.getKey())
                    }
                }
            }
        }
    }

    private fun requestOtp() {
        lifecycleScope.launch {
            viewModel.linkCareContextModel.hipId.let { hipId ->
                viewModel.checkForBlockedState(hipId).collect {
                    when (it) {
                        OtpCallState.OtpReqAvailable -> {
                            viewModel.generateCareContextOtp()
                        }

                        is OtpCallState.OtpReqBlocked -> {
                            viewModel.otpRequestBlocked.value = it.otpRequestCallModel
                            binding.resendCCOtp.isEnabled = false
                        }
                    }
                }

            }
        }

    }

    private fun observeUiState() {
        lifecycleScope.launch(Dispatchers.Main) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    when (it) {
                        is GenerateAbhaUiState.CCVerifyOtpRequested -> {
                            binding.otpEt.isEnabled = false
                            binding.resendCCOtp.isEnabled = false
                            binding.verifyCCOtp.isEnabled = false
                            binding.startAuth.isEnabled = false
                            viewModel.uiState.emit(GenerateAbhaUiState.InvalidState)
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(true))
                        }

                        is GenerateAbhaUiState.CCOtpRequested -> {
                            binding.otpEt.isEnabled = false
                            binding.resendCCOtp.isEnabled = false
                            binding.verifyCCOtp.isEnabled = false
                            binding.startAuth.isEnabled = false
                            binding.timeProgress.startTimer()
                            viewModel.uiState.emit(GenerateAbhaUiState.InvalidState)
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(true))
                        }

                        is GenerateAbhaUiState.Success -> {
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(false))

                            when (it.requestType) {
                                RequestType.CC_AUTH_INIT -> {
                                    binding.otpEt.requestFocus()
                                    binding.otpEt.isEnabled = true
                                    binding.startAuth.isEnabled = true

                                    val ccGenerateOtpResponseModel = Gson().fromJson(
                                        it.data,
                                        CCGenerateOtpResponseModel::class.java
                                    )


                                    viewModel.confirmAuthModel =
                                        ConfirmAuthModel(
                                            ccGenerateOtpResponseModel.transactionId,
                                            Credential()
                                        )

                                }

                                RequestType.CC_AUTH_CONFIRM -> {
                                    val ccVerifyOtpResponseModel = Gson().fromJson(
                                        it.data,
                                        CCVerifyOtpResponseModel::class.java
                                    )

                                    navigateToLinkCCScreen(ccVerifyOtpResponseModel.accessToken)

                                }

                                else -> {
                                    //Exhaustive block
                                }
                            }
                        }

                        is GenerateAbhaUiState.AbdmError -> {

                            when (it.requestType) {
                                RequestType.CC_AUTH_CONFIRM -> {
                                    binding.verifyCCOtp.isEnabled = true
                                    binding.startAuth.isEnabled = true
                                }

                                else -> {

                                }
                            }
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(false))
                            (activity as AbdmActivity).showBlockerDialog(it.data.getErrorMsg())

                        }

                        is GenerateAbhaUiState.Error -> {
                            when (it.requestType) {
                                RequestType.CC_AUTH_CONFIRM -> {
                                    binding.verifyCCOtp.isEnabled = true
                                    binding.startAuth.isEnabled = true
                                }

                                else -> {

                                }
                            }
                            (activity as AbdmActivity).showBlockerDialog(it.data.get("message").asString)
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(false))
                        }

                        else -> {
                            //Exhaustive block
                        }

                    }

                }
            }
        }
    }

    override fun onClick(view: View?) {
        super.onClick(view)
        when (view?.id) {
            R.id.resendCCOtp -> {
                requestOtp()
            }

            R.id.verifyCCOtp -> {
                viewModel.confirmAuthModel.credential.authCode = binding.otpEt.text.toString()
                viewModel.confirmCareContextOtp()
            }

            R.id.startAuth -> {
                viewModel.confirmAuthModel.credential.demographic =
                    viewModel.linkCareContextModel.patient.demographics
                viewModel.confirmCareContextOtp()
            }
        }
    }

    private fun navigateToLinkCCScreen(accessToken: String) {
        val bundle = bundleOf(
            "accessToken" to accessToken
        )
        findNavController().navigate(
            R.id.action_verifyCCLinkOtpFragment_to_linkContextCareFragment,
            bundle
        )
    }

}