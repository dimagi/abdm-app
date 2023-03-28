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
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.commcare.dalvik.abha.R
import org.commcare.dalvik.abha.databinding.VerifyMobileOtpBinding
import org.commcare.dalvik.abha.ui.main.activity.AbdmActivity
import org.commcare.dalvik.abha.ui.main.activity.VerificationMode
import org.commcare.dalvik.abha.ui.main.custom.OtpTimerState
import org.commcare.dalvik.abha.utility.AppConstants
import org.commcare.dalvik.abha.utility.observeText
import org.commcare.dalvik.abha.viewmodel.GenerateAbhaUiState
import org.commcare.dalvik.abha.viewmodel.AbdmViewModel
import org.commcare.dalvik.abha.viewmodel.OtpCallState
import org.commcare.dalvik.abha.viewmodel.RequestType
import org.commcare.dalvik.domain.model.VerifyOtpRequestModel
import org.commcare.dalvik.data.util.PrefKeys
import org.commcare.dalvik.domain.model.AbhaDetailModel
import org.commcare.dalvik.domain.model.AbhaVerificationResultModel
import org.commcare.dalvik.domain.model.OtpResponseModel
import timber.log.Timber

@AndroidEntryPoint
class VerifyMobileOtpFragment :
    BaseFragment<VerifyMobileOtpBinding>(VerifyMobileOtpBinding::inflate) {

    private val viewModel: AbdmViewModel by activityViewModels()


    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.model = viewModel
        binding.clickHandler = this
        observeOtpTimer()
        observeUiState()

        lifecycleScope.launch(Dispatchers.Main) {
            binding.mobileOtpEt.observeText().collect {
                binding.verifyOtp.isEnabled = it == AppConstants.MOBILE_OTP_LENGTH
            }
        }

        /**
         * Request for OTP
         */
        if (hasNetworkConnectivity()) {
            arguments?.getSerializable("verificationMode")?.let {
                it as VerificationMode
                when (it) {
                    VerificationMode.VERIFY_MOBILE_OTP -> {
                        requestMobileOtp()
                    }
                    VerificationMode.CONFIRM_MOBILE_OTP -> {
                        requestMobileAuthOtp()
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
     * Request MOBILE_OTP
     */
    private fun requestMobileOtp() {
        lifecycleScope.launch{
            viewModel.abhaRequestModel.value?.aadhaar?.let { aadhaarKey ->
                viewModel.checkForBlockedState(aadhaarKey).collect {
                    when (it) {
                        OtpCallState.OtpReqAvailable -> {
                            viewModel.requestMobileOtp()
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
     * Request MOBILE AUTH_OTP
     */
    private fun requestMobileAuthOtp() {
        lifecycleScope.launch{
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
     * Verify MOBILE OTP
     */
    private fun verifyMobileOtp() {
        lifecycleScope.launch {
            viewModel.abhaRequestModel.value?.aadhaar?.let { aadhaarKey ->
                viewModel.checkForBlockedState(aadhaarKey).collect {
                    when (it) {
                        OtpCallState.OtpReqAvailable -> {
                            viewModel.verifyMobileOtp(getMobileOtpRequestModel())
                        }
                        is OtpCallState.OtpReqBlocked -> {
                            binding.verifyOtp.isEnabled = false
//                            viewModel.otpRequestBlocked.value = it.otpRequestCallModel
                        }
                    }
                }
            }
        }
    }

    /**
     * Confirm MOBILE OTP
     */
    private fun confirmMobileAuthOtp() {
        lifecycleScope.launch {
            arguments?.getString("abhaId")?.let { aadhaarKey ->
                viewModel.checkForBlockedState(aadhaarKey).collect {
                    when (it) {
                        OtpCallState.OtpReqAvailable -> {
                            viewModel.confirmMobileAuthOtp(getMobileOtpRequestModel())
                        }
                        is OtpCallState.OtpReqBlocked -> {
                            binding.verifyOtp.isEnabled = false
//                            viewModel.otpRequestBlocked.value = it.otpRequestCallModel
                        }
                    }
                }
            }
        }
    }

    private fun observeUiState() {
        lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    Timber.d("EMIT Received Mobile-> ${it}")
                    when (it) {

                        GenerateAbhaUiState.MobileOtpRequested,
                        GenerateAbhaUiState.AuthOtpRequested,
                        GenerateAbhaUiState.VerifyAuthOtpRequested,
                        GenerateAbhaUiState.VerifyMobileOtpRequested -> {
                            Timber.d("--------- MOBILE OTP GEN / VERIFY REQUESTED -----------")
                            binding.mobileOtpEt.isEnabled = false
                            binding.resentOtp.isEnabled = false
                            binding.verifyOtp.isEnabled = false
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(true))
                        }

                        /**
                         * SUCCESS
                         */
                        is GenerateAbhaUiState.Success -> {
                            when (it.requestType) {
                                /**
                                 * REQUESTS
                                 */
                                RequestType.GENERATE_AUTH_OTP -> {
                                    binding.mobileOtpEt.isEnabled = true
                                    binding.timeProgress.startTimer()
                                    val otResponseModel =
                                        Gson().fromJson(it.data, OtpResponseModel::class.java)
                                    viewModel.abhaRequestModel.value?.txnId = otResponseModel.txnId
//                                    viewModel.abhaRequestModel.value?.abhaId?.let {abhaIdKey ->
//                                        viewModel.clearBlockState(abhaIdKey)
//                                    }
                                }

                                RequestType.MOBILE_OTP -> {
                                    binding.mobileOtpEt.isEnabled = true
                                    binding.timeProgress.startTimer()

//                                    viewModel.abhaRequestModel.value?.aadhaar?.let {aadhaarKey ->
//                                        viewModel.clearBlockState(aadhaarKey)
//                                    }

                                }

                                /**
                                 * VERIFICATION
                                 */
                                RequestType.CONFIRM_AUTH_MOBILE_OTP -> {
                                    val abhaVerificationResultModel = Gson().fromJson(
                                        it.data,
                                        AbhaVerificationResultModel::class.java
                                    )
                                    arguments?.getString("abhaId")?.let {
                                        abhaVerificationResultModel.healthId = it
                                    }
                                    val bundle =
                                        bundleOf("resultModel" to abhaVerificationResultModel)
                                    navigateToNextScreen(
                                        RequestType.CONFIRM_AUTH_MOBILE_OTP,
                                        bundle
                                    )
                                }


                                RequestType.MOBILE_OTP_VERIFY -> {
                                    binding.mobileOtpEt.isEnabled = true
                                    if (binding.timeProgress.timeState.value != OtpTimerState.TimerStarted) {
                                        binding.resentOtp.isEnabled = true
                                    }

                                    val abhaDetailModel =
                                        Gson().fromJson(it.data, AbhaDetailModel::class.java)
                                    abhaDetailModel.data = it.data
                                    viewModel.abhaDetailModel.value = abhaDetailModel
                                    navigateToNextScreen(RequestType.MOBILE_OTP_VERIFY)
                                }
                            }
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(false))
                        }

                        /**
                         * ERROR
                         */
                        is GenerateAbhaUiState.Error -> {
                            when (it.requestType) {
                                RequestType.GENERATE_AUTH_OTP ->{
                                    binding.mobileOtpEt.isEnabled = true
                                    binding.timeProgress.startTimer()
//                                    viewModel.abhaRequestModel.value?.abhaId?.let { abhaIdKey ->
//                                        startResendTimer(abhaIdKey)
//                                    }
                                }

                                RequestType.MOBILE_OTP -> {
                                    binding.mobileOtpEt.isEnabled = true
                                    binding.timeProgress.startTimer()
//                                    viewModel.abhaRequestModel.value?.aadhaar?.let { aadhaarKey ->
//                                        startResendTimer(aadhaarKey)
//                                    }
                                }

                                RequestType.CONFIRM_AUTH_MOBILE_OTP,
                                RequestType.MOBILE_OTP_VERIFY -> {
                                    binding.mobileOtpEt.isEnabled = false
                                    if (binding.timeProgress.timeState.value != OtpTimerState.TimerStarted) {
                                        binding.resentOtp.isEnabled = true
                                    }
                                    binding.mobileOtpEt.setText("")
                                }
                            }
                            (activity as AbdmActivity).showBlockerDialog(it.data.get("message").asString)
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(false))
                        }

                        is GenerateAbhaUiState.AbdmError -> {
                            when (it.requestType) {
                                RequestType.GENERATE_AUTH_OTP ->{
                                    binding.timeProgress.startTimer()
//                                    viewModel.abhaRequestModel.value?.abhaId?.let { abhaIdKey ->
//                                        startResendTimer(abhaIdKey)
//                                    }
                                }

                                RequestType.MOBILE_OTP -> {
                                    binding.timeProgress.startTimer()
//                                    viewModel.abhaRequestModel.value?.aadhaar?.let { aadhaarKey ->
//                                        startResendTimer(aadhaarKey)
//                                    }
                                }

                                RequestType.CONFIRM_AUTH_MOBILE_OTP,
                                RequestType.MOBILE_OTP_VERIFY -> {
                                    binding.mobileOtpEt.setText("")
                                    binding.mobileOtpEt.isEnabled = true
                                    if (binding.timeProgress.timeState.value != OtpTimerState.TimerStarted) {
                                        binding.resentOtp.isEnabled = true
                                    }
                                }
                            }
                            (activity as AbdmActivity).showBlockerDialog(it.data.getActualMessage())
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(false))
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

    private fun getMobileOtpRequestModel() = VerifyOtpRequestModel(
        viewModel.abhaRequestModel.value!!.txnId,
        binding.mobileOtpEt.text.toString(),
        arguments?.getString("healthId")
    )


    override fun onClick(view: View?) {
        super.onClick(view)
        if(!hasNetworkConnectivity()){
            return
        }
        val verificationMode = arguments?.getSerializable("verificationMode")
        when (view?.id) {
            R.id.resentOtp -> {
                when (verificationMode) {
                    VerificationMode.CONFIRM_MOBILE_OTP -> {
                        requestMobileAuthOtp()
                    }
                    else -> {
                        binding.verifyOtp.isEnabled = false
                        binding.resentOtp.isEnabled = false
                        requestMobileOtp()
                    }
                }
            }

            R.id.verifyOtp -> {
                when (verificationMode) {
                    VerificationMode.CONFIRM_MOBILE_OTP -> {
                        viewModel.confirmMobileAuthOtp(getMobileOtpRequestModel())
                    }
                    else -> {
                        viewModel.verifyMobileOtp(getMobileOtpRequestModel())
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
                    R.id.action_verifyMobileOtpFragment_to_abhaVerificationResultFragment,
                    bundle
                )
            }

            RequestType.MOBILE_OTP_VERIFY -> {
                viewModel.abhaRequestModel.value?.aadhaar?.let {
                    viewModel.clearOtpRequestState(it)
                }
                findNavController().navigate(R.id.action_verifyMobileOtpFragment_to_abhaDetailFragment)
            }

        }
    }
}
