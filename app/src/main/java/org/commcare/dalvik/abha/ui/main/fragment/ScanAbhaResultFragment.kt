package org.commcare.dalvik.abha.ui.main.fragment

import android.content.Intent
import android.os.Bundle
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
import org.commcare.dalvik.abha.databinding.ScanAbhaResultBinding
import org.commcare.dalvik.abha.model.AbhaScanModel
import org.commcare.dalvik.abha.ui.main.activity.AbdmActivity
import org.commcare.dalvik.abha.ui.main.activity.AbdmResponseCode
import org.commcare.dalvik.abha.viewmodel.GenerateAbhaUiState
import org.commcare.dalvik.abha.viewmodel.ScanAbhaViewModel
import timber.log.Timber

class ScanAbhaResultFragment : BaseFragment<ScanAbhaResultBinding>(ScanAbhaResultBinding::inflate) {

    val viewModel: ScanAbhaViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.clickHandler = this
        binding.viewModel = viewModel

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        observeUiState()
        viewModel.checkForAbhaAvailability()

    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    when (it) {

                        is GenerateAbhaUiState.Success -> {
                            binding.dispatchScanResult.isEnabled = true
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(false))
                            viewModel.abhaScanModel.validationState =  AbhaScanModel.AbhaValidationState.VALID
                            binding.verificationStatus.text = viewModel.abhaScanModel.validationState.value
                            binding.verificationStatus.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.green
                                )
                            )
                        }

                        is GenerateAbhaUiState.Error -> {
                            binding.dispatchScanResult.isEnabled = true
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(false))
                            viewModel.abhaScanModel.validationState =  AbhaScanModel.AbhaValidationState.INVALID
                            binding.verificationStatus.text = viewModel.abhaScanModel.validationState.value
                            binding.verificationStatus.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.red
                                )
                            )
                        }

                        is GenerateAbhaUiState.AbdmError -> {
                            binding.dispatchScanResult.isEnabled = true
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(false))
                            viewModel.abhaScanModel.validationState =  AbhaScanModel.AbhaValidationState.INVALID
                            binding.verificationStatus.text = viewModel.abhaScanModel.validationState.value
                            binding.verificationStatus.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.red
                                )
                            )
                        }

                        else -> {
                            //exhaustive block
                        }
                    }
                }
            }
        }
    }

    override fun onClick(view: View?) {
        super.onClick(view)
        when(view?.id){
            R.id.dispatchScanResult ->{
                dispatchResult()
            }
        }

    }

    private fun dispatchResult() {
        val intent = Intent().apply {
            putExtra("scanData", Gson().toJson(binding.viewModel?.abhaScanModel))
            putExtra("code", AbdmResponseCode.SUCCESS.value)
            putExtra("message", "ABHA scan completed.")
        }
        (activity as AbdmActivity).onAbhaScanCompleted(intent)
    }

}