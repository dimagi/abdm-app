package org.commcare.dalvik.abha.ui.main.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.commcare.dalvik.abha.R
import org.commcare.dalvik.abha.databinding.AbhaDetailBinding
import org.commcare.dalvik.abha.databinding.KeyValueBinding
import org.commcare.dalvik.abha.ui.main.activity.AbdmActivity
import org.commcare.dalvik.abha.ui.main.activity.AbdmResponseCode
import org.commcare.dalvik.abha.utility.AppConstants
import org.commcare.dalvik.abha.viewmodel.AbdmViewModel
import org.commcare.dalvik.abha.viewmodel.GenerateAbhaUiState
import org.commcare.dalvik.domain.model.HealthCardResponseModel

class AbhaCreationResultFragment : BaseFragment<AbhaDetailBinding>(AbhaDetailBinding::inflate) {
    private val viewModel: AbdmViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AbdmActivity).hideMenu()
        binding.clickHandler = this
        binding.model = viewModel.abhaDetailModel.value
//        renderAadhaarData()
        renderAbhaCard()

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                dispatchResult()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        (activity as AbdmActivity).hideBack()

        observeUiState()

//        viewModel.abhaRequestModel.value?.aadhaar?.let {
//            viewModel.clearOtpRequestState(it)
//        }
    }


    private fun renderAbhaCard() {
        viewModel.abhaDetailModel.value?.userToken?.let {
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

    private fun renderAadhaarData() {
        viewModel.abhaDetailModel.value?.getAadhaarDataList(
            requireContext(),
            AppConstants.abhaHealthLangKeysMap
        )?.forEachIndexed { index, kvModel ->
            val kvBinding = KeyValueBinding.inflate(LayoutInflater.from(requireContext()))
            if (index % 2 == 0) {
                kvBinding.tableRow.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.white
                    )
                )
            } else {
                kvBinding.tableRow.setBackgroundColor(
                    ContextCompat.getColor(
                        requireContext(),
                        R.color.grey_lighter
                    )
                )
            }
            kvBinding.model = kvModel
            binding.aadhaarDataTableLayout.addView(kvBinding.root)
        }
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
            putExtra("verified", "true")
            putExtra("message", "ABHA creation completed.")
            putExtra("exists_on_abdm", binding.model?.existsOnAbdm.toString())
            putExtra("exists_on_hq", binding.model?.existsOnHq.toString())
            if (binding.shareWithCC.isChecked) {
                putExtra("aadhaarData", binding.model?.data?.toString())
            }
        }
        (activity as AbdmActivity).onAbhaNumberReceived(intent)
    }


}