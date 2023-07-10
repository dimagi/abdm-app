package org.commcare.dalvik.abha.ui.main.fragment

import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.fragment.app.viewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.commcare.dalvik.abha.R
import org.commcare.dalvik.abha.databinding.ScanAbhaResultBinding
import org.commcare.dalvik.abha.viewmodel.GenerateAbhaUiState
import org.commcare.dalvik.abha.viewmodel.OtpCallState
import org.commcare.dalvik.abha.viewmodel.ScanAbhaViewModel
import org.commcare.dalvik.domain.model.CheckAbhaResponseModel

class ScanAbhaResultFragment : BaseFragment<ScanAbhaResultBinding>(ScanAbhaResultBinding::inflate) {

    val viewModel: ScanAbhaViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.clickHandler = this
        binding.scanModel = viewModel.abhaScanModel.value

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
                        is GenerateAbhaUiState.Loading -> {

                        }

                        is GenerateAbhaUiState.Success -> {
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(false))
                            binding.verificationStatus.text = "VERIFIED"
                            binding.verificationStatus.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.green
                                )
                            )
                        }

                        is GenerateAbhaUiState.Error -> {
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(false))
                            binding.verificationStatus.text = "NOT VERIFIED"
                            binding.verificationStatus.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.red
                                )
                            )
                        }
                    }
                }
            }
        }
    }

    override fun onClick(view: View?) {
        super.onClick(view)

    }
}