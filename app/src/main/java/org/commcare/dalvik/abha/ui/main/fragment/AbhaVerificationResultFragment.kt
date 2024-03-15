package org.commcare.dalvik.abha.ui.main.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.commcare.dalvik.abha.databinding.AbhaVerificationResultBinding
import org.commcare.dalvik.abha.ui.main.activity.AbdmActivity
import org.commcare.dalvik.abha.ui.main.activity.AbdmResponseCode
import org.commcare.dalvik.abha.viewmodel.AbdmViewModel
import org.commcare.dalvik.abha.viewmodel.GenerateAbhaUiState
import org.commcare.dalvik.domain.model.AbhaVerificationResultModel
import org.commcare.dalvik.domain.model.HealthCardResponseModel

class AbhaVerificationResultFragment :
    BaseFragment<AbhaVerificationResultBinding>(AbhaVerificationResultBinding::inflate) {

    private val viewModel: AbdmViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AbdmActivity).hideMenu()
        binding.clickHandler = this
        arguments?.getSerializable("resultModel")?.let {
            it as AbhaVerificationResultModel
            binding.model = it
        }

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                dispatchResult()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        (activity as AbdmActivity).hideBack()

//        viewModel.abhaRequestModel.value?.abhaId?.let {
//            viewModel.clearOtpRequestState(it)
//        }
        renderAbhaCard()
        observeUiState()
    }

    override fun onClick(view: View?) {
        super.onClick(view)
        dispatchResult()
    }

    private fun dispatchResult() {
        val intent = Intent().apply {
            putExtra("abha_id", binding.model?.healthIdNumber)
            putExtra("abha_address", binding.model?.healthId)
            putExtra("code", AbdmResponseCode.SUCCESS.value)
            putExtra("verified", binding.model?.status)
            putExtra("aadhaarData", binding.model?.aadharData?.toString())
            putExtra("message", "ABHA verification completed.")
        }

        (activity as AbdmActivity).onAbhaNumberVerification(intent)
    }

    private fun renderAbhaCard() {
        binding.model?.userToken?.let {
            viewModel.fetchAbhaCard(it)
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    when (it) {
                        is GenerateAbhaUiState.Success -> {
                            binding.healthCardModel =
                                Gson().fromJson(it.data, HealthCardResponseModel::class.java)
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(false))
                        }

                        is GenerateAbhaUiState.Error -> {
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(false))
                        }

                        is GenerateAbhaUiState.AbdmError -> {
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(false))
                            (activity as AbdmActivity).showBlockerDialog(it.data.getActualMessage())
                        }
                        else -> {
                            //exhaustive block
                        }

                    }
                }
            }
        }
    }
}