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
import org.commcare.dalvik.abha.databinding.VerifyAadhaarOtpBinding
import org.commcare.dalvik.abha.ui.main.activity.AbdmActivity
import org.commcare.dalvik.abha.ui.main.activity.VerificationMode
import org.commcare.dalvik.abha.ui.main.custom.OtpTimerState
import org.commcare.dalvik.abha.utility.AppConstants
import org.commcare.dalvik.abha.utility.observeText
import org.commcare.dalvik.abha.viewmodel.GenerateAbhaUiState
import org.commcare.dalvik.abha.viewmodel.AbdmViewModel
import org.commcare.dalvik.abha.viewmodel.OtpCallState
import org.commcare.dalvik.abha.viewmodel.RequestType
import org.commcare.dalvik.data.util.PrefKeys
import org.commcare.dalvik.domain.model.AbhaVerificationResultModel
import org.commcare.dalvik.domain.model.OtpResponseModel
import org.commcare.dalvik.domain.model.VerifyOtpRequestModel
import timber.log.Timber

class VerifyAadhaarOtpFragment :
    BaseFragment<VerifyAadhaarOtpBinding>(VerifyAadhaarOtpBinding::inflate) {

    private val viewModel: AbdmViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.model = viewModel
        binding.clickHandler = this
        observeOtpTimer()
        viewModel.resetUiState()
        observeUiState()

        lifecycleScope.launch(Dispatchers.Main) {
            binding.aadhaarOtpEt.observeText().collect {
                binding.verifyOtp.isEnabled = it == AppConstants.AADHAAR_OTP_LENGTH
            }
        }

        /**
         * Request for OTP
         */
        if (hasNetworkConnectivity()) {
            arguments?.getSerializable("verificationMode")?.let {
                it as VerificationMode
                when (it) {
                    VerificationMode.VERIFY_AADHAAR_OTP -> {
                        requestAadhaarOtp()
                    }
                    VerificationMode.CONFIRM_AADHAAR_OTP -> {
                        requestAadhaarAuthOtp()
                    }
                    else -> {
                        //exhaustive block
                    }
                }
            }
        }

    }

    /**
     * Start Resend timer
     */

    private fun startResendTimer(key:String){
        lifecycleScope.launch{
            viewModel.checkForBlockedState(key).collect {
                when (it) {
                    OtpCallState.OtpReqAvailable -> {
                        binding.timeProgress.startTimer()
                    }
                    is OtpCallState.OtpReqBlocked -> {
                        viewModel.otpRequestBlocked.value = it.otpRequestCallModel
                    }
                }
            }
        }
    }

    /**
     * Request AADHAAR OTP
     */
    private fun requestAadhaarOtp() {
        lifecycleScope.launch {
            viewModel.abhaRequestModel.value?.aadhaar?.let { aadhaarKey ->
                viewModel.checkForBlockedState(aadhaarKey).collect {
                    when (it) {
                        OtpCallState.OtpReqAvailable -> {
                            viewModel.requestAadhaarOtp()
                        }
                        is OtpCallState.OtpReqBlocked -> {
                            viewModel.otpRequestBlocked.value = it.otpRequestCallModel
                        }
                    }
                }
            }
        }
    }

    /**
     * Verify AADHAAR OTP
     */
    private fun verifyAadhaarOtp() {
        lifecycleScope.launch {
            viewModel.abhaRequestModel.value?.aadhaar?.let { aadhaarKey ->
                viewModel.getOtpRequestCount(aadhaarKey).collect { otpCount ->
                    if (otpCount < 5) {
                        viewModel.verifyAadhaarOtp(getAadhaarOtpVeriyModel())
                    } else {
                        binding.verifyOtp.isEnabled = false
                    }
                }
            }
        }
    }

    /**
     * Confirm AADHAAR AUTH OTP
     */
    private fun confirmAadhaarAuthOtp() {
        lifecycleScope.launch {
            arguments?.getString("abhaId")?.let { abhaId ->

                viewModel.getOtpRequestCount(abhaId).collect { otpCount ->
                    if (otpCount < 5) {
                        viewModel.confirmAadhaarAuthOtp(getAadhaarOtpVeriyModel())
                    } else {
                        binding.verifyOtp.isEnabled = false
                    }
                }
            }
        }
    }

    /**
     * Request AADHAAR AUTH_OTP
     */
    private fun requestAadhaarAuthOtp() {
        lifecycleScope.launch {
            arguments?.getString("abhaId")?.let { healthId ->
                viewModel.selectedAuthMethod?.let { selectedAuthMethod ->
                    viewModel.checkForBlockedState(healthId).collect {
                        when (it) {
                            OtpCallState.OtpReqAvailable -> {
                                viewModel.getAuthOtp(healthId, selectedAuthMethod)
                            }
                            is OtpCallState.OtpReqBlocked -> {
                                viewModel.otpRequestBlocked.value = it.otpRequestCallModel
                            }
                        }
                    }
                }
            }
        }
    }

    /**
     * Observer UI STATE
     */
    private fun observeUiState() {
        lifecycleScope.launch(Dispatchers.Main) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    Timber.d("EMIT Received AADHAAR-> ${it}")
                    when (it) {

                        GenerateAbhaUiState.AuthOtpRequested,
                        GenerateAbhaUiState.AadhaarOtpRequested,
                        GenerateAbhaUiState.VerifyAuthOtpRequested,
                        GenerateAbhaUiState.VerifyAadhaarOtpRequested -> {
                            Timber.d("--------- VERIFY AADHAAR OTP REQUESTED-----------")
                            binding.resentOtp.isEnabled = false
                            binding.verifyOtp.isEnabled = false
                            binding.aadhaarOtpEt.isEnabled = false
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(true))
                        }

                        /**
                         * SUCCESS
                         */
                        is GenerateAbhaUiState.Success -> {
                            when (it.requestType) {
                                /**
                                 * OTP REQUEST
                                 */
                                RequestType.AADHAAR_OTP -> {
                                    binding.aadhaarOtpEt.requestFocus()
                                    binding.aadhaarOtpEt.isEnabled = true
                                    binding.timeProgress.startTimer()
                                    val otResponseModel =
                                        Gson().fromJson(it.data, OtpResponseModel::class.java)
                                    viewModel.abhaRequestModel.value?.txnId = otResponseModel.txnId

//                                    viewModel.abhaRequestModel.value?.aadhaar?.let { aadhaarKey ->
//                                        viewModel.clearBlockState(aadhaarKey)
//                                    }
                                }
                                RequestType.GENERATE_AUTH_OTP -> {
                                    binding.aadhaarOtpEt.requestFocus()
                                    val otResponseModel =
                                        Gson().fromJson(it.data, OtpResponseModel::class.java)
                                    viewModel.abhaRequestModel.value?.txnId = otResponseModel.txnId
                                    binding.aadhaarOtpEt.isEnabled = true
                                    binding.timeProgress.startTimer()

//                                    viewModel.abhaRequestModel.value?.abhaId?.let { abhaId ->
//                                        viewModel.clearBlockState(abhaId)
//                                    }
                                }

                                /**
                                 * OTP VERIFICATION
                                 */
                                RequestType.CONFIRM_AUTH_AADHAAR_OTP -> {
                                    val abhaVerificationResultModel = Gson().fromJson(
                                        it.data,
                                        AbhaVerificationResultModel::class.java
                                    )
//                                    arguments?.getString("abhaId")?.let {
//                                        abhaVerificationResultModel.healthId = it
//                                    }

                                    val bundle =
                                        bundleOf("resultModel" to abhaVerificationResultModel)
                                    navigateToNextScreen(
                                        RequestType.CONFIRM_AUTH_MOBILE_OTP,
                                        bundle
                                    )
                                }

                                RequestType.AADHAAR_OTP_VERIFY -> {
                                    navigateToNextScreen(RequestType.AADHAAR_OTP_VERIFY)
                                }

                                else -> {
                                    //exhaustive block
                                }
                            }
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(false))
                        }

                        /**
                         * ERROR
                         */

                        is GenerateAbhaUiState.Error -> {
                            when (it.requestType) {

                                RequestType.GENERATE_AUTH_OTP -> {
                                    binding.verifyOtp.isEnabled =
                                        binding.aadhaarOtpEt.text?.isNotEmpty() ?: false
                                    binding.timeProgress.startTimer()
//                                    viewModel.abhaRequestModel.value?.abhaId?.let { abhaIdKey ->
//                                        startResendTimer(abhaIdKey)
//                                    }
                                }

                                RequestType.AADHAAR_OTP -> {
                                    binding.verifyOtp.isEnabled =
                                        binding.aadhaarOtpEt.text?.isNotEmpty() ?: false
                                    binding.timeProgress.startTimer()
//                                    viewModel.abhaRequestModel.value?.aadhaar?.let { aadhaarKey ->
//                                        startResendTimer(aadhaarKey)
//                                    }
                                }

                                RequestType.CONFIRM_AUTH_AADHAAR_OTP,
                                RequestType.AADHAAR_OTP_VERIFY -> {
                                    binding.aadhaarOtpEt.setText("")
                                    binding.aadhaarOtpEt.isEnabled = true
                                    if (binding.timeProgress.timeState.value != OtpTimerState.TimerStarted) {
                                        binding.resentOtp.isEnabled = true
                                    }
                                }
                                else -> {
                                    //exhaustive block
                                }

                            }
                            (activity as AbdmActivity).showBlockerDialog(it.data.get("message").asString)
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(false))
                        }

                        /**
                         * ABDM ERROR
                         */
                        is GenerateAbhaUiState.AbdmError -> {
                            when (it.requestType) {
                                RequestType.GENERATE_AUTH_OTP -> {
                                    binding.verifyOtp.isEnabled =
                                        binding.aadhaarOtpEt.text?.isNotEmpty() ?: false
                                    binding.timeProgress.startTimer()

//                                    viewModel.abhaRequestModel.value?.abhaId?.let { abhaIdKey ->
//                                        startResendTimer(abhaIdKey)
//                                    }
                                }

                                RequestType.AADHAAR_OTP -> {
                                    binding.verifyOtp.isEnabled =
                                        binding.aadhaarOtpEt.text?.isNotEmpty() ?: false
                                    binding.timeProgress.startTimer()

//                                    viewModel.abhaRequestModel.value?.aadhaar?.let { aadhaarKey ->
//                                        startResendTimer(aadhaarKey)
//                                    }
                                }

                                RequestType.CONFIRM_AUTH_AADHAAR_OTP,
                                RequestType.AADHAAR_OTP_VERIFY -> {
                                    binding.aadhaarOtpEt.setText("")
                                    binding.aadhaarOtpEt.isEnabled = true
                                    if (binding.timeProgress.timeState.value != OtpTimerState.TimerStarted) {
                                        binding.resentOtp.isEnabled = true
                                    }
                                }
                                else -> {
                                    //exhaustive block
                                }
                            }
                            (activity as AbdmActivity).showBlockerDialog(it.data.getActualMessage())
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(false))
                        }
                        else -> {
                            //exhaustive block
                        }
                    }
                }
            }
        }
    }

    private fun observeOtpTimer() {
        lifecycleScope.launch(Dispatchers.Main) {
            binding.timeProgress.timeState.collect {
                when (it) {
                    OtpTimerState.None -> {
                        // Nothing for now
                    }
                    OtpTimerState.TimerStarted -> {
                        binding.resentOtp.isEnabled = false
                    }
                    OtpTimerState.TimerOver -> {
                        binding.resentOtp.isEnabled = true
                        viewModel.getData(PrefKeys.OTP_BLOCKED_TS.getKey())
                    }
                }
            }
        }
    }

    private fun getAadhaarOtpVeriyModel() = VerifyOtpRequestModel(
        viewModel.abhaRequestModel.value?.txnId!!,
        binding.aadhaarOtpEt.text.toString()
    )

    override fun onClick(view: View?) {
        super.onClick(view)
        if (!hasNetworkConnectivity()) {
            return
        }
        val verificationMode = arguments?.getSerializable("verificationMode")
        when (view?.id) {
            R.id.resentOtp -> {
                when (verificationMode) {
                    VerificationMode.CONFIRM_AADHAAR_OTP -> {
                        requestAadhaarAuthOtp()
                    }
                    VerificationMode.VERIFY_AADHAAR_OTP -> {
                        requestAadhaarOtp()
                    }
                }

            }

            R.id.verifyOtp -> {
                when (verificationMode) {
                    VerificationMode.CONFIRM_AADHAAR_OTP -> {
                        viewModel.confirmAadhaarAuthOtp(getAadhaarOtpVeriyModel())
                    }
                    VerificationMode.VERIFY_AADHAAR_OTP -> {
                        viewModel.verifyAadhaarOtp(getAadhaarOtpVeriyModel())
                    }
                    else -> {
                        //exhaustive block
                    }
                }
            }

        }
    }

    private fun navigateToNextScreen(srcRequestType: RequestType, bundle: Bundle = bundleOf()) {
        when (srcRequestType) {
            RequestType.CONFIRM_AUTH_MOBILE_OTP -> {
                viewModel.abhaRequestModel.value?.abhaId?.let {
                    viewModel.clearOtpRequestState(it)
                }
                findNavController().navigate(
                    R.id.action_verifyAadhaarOtpFragment_to_abhaVerificationResultFragment,
                    bundle
                )
            }
            RequestType.AADHAAR_OTP_VERIFY -> {
                viewModel.abhaRequestModel.value?.aadhaar?.let {
                    viewModel.clearOtpRequestState(it)
                }
                val bundle = bundleOf("verificationMode" to VerificationMode.VERIFY_MOBILE_OTP)
                bundle.putString("healthId",arguments?.getString("healthId"))
                findNavController().navigate(
                    R.id.action_verifyAadhaarOtpFragment_to_verifyMobileOtpFragment,
                    bundle
                )
            }
            else -> {
                //exhaustive block
            }

        }

    }

}