package org.commcare.dalvik.abha.ui.main.fragment

import android.os.Bundle
import android.text.TextUtils
import android.text.method.ScrollingMovementMethod
import android.view.View
import androidx.core.widget.addTextChangedListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.commcare.dalvik.abha.R
import org.commcare.dalvik.abha.databinding.DisclaimerBinding
import org.commcare.dalvik.abha.ui.main.activity.AbdmActivity
import org.commcare.dalvik.abha.utility.DialogUtility
import org.commcare.dalvik.abha.viewmodel.AbdmViewModel
import org.commcare.dalvik.abha.viewmodel.GenerateAbhaUiState
import org.commcare.dalvik.domain.model.CheckAbhaResponseModel


class AbhaDisclaimerFragment : BaseFragment<DisclaimerBinding>(DisclaimerBinding::inflate) {

    private val viewModel: AbdmViewModel by activityViewModels()

    override fun onClick(view: View?) {
        super.onClick(view)
        when (view?.id) {
            R.id.acceptDisclaimer -> {
                binding.disclaimerLayout.visibility = View.GONE
                binding.abhaAddressCheckLayout.visibility = View.VISIBLE
                (activity as AbdmActivity).setToolbarTitle(R.string.checkABHA)
            }
            R.id.declineDisclaimer -> {
                (activity as AbdmActivity).showMessageAndDispatchResult("Consent declined by user.")
            }
            R.id.checkABHA -> {
                val abhaAddress = binding.abhaNumberEt.text.toString()
                binding.abhaNumInputLayout.helperText = ""
                binding.abhaNumberEt.isEnabled = false
                binding.checkABHA.isEnabled = false
                binding.skipCheckABHA.isEnabled = false
                viewModel.checkForAbhaAvailability(abhaAddress+"@abdm")
            }
            R.id.skipCheckABHA ->{
                navigateToAadhaarOtpVerificationScreen(null)
            }

        }

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
                            binding.checkABHA.isEnabled = true
                            binding.abhaNumberEt.isEnabled = true
                            binding.skipCheckABHA.isEnabled = true
                            val checkAbhaResponseModel =
                                Gson().fromJson(it.data, CheckAbhaResponseModel::class.java)
                            if (checkAbhaResponseModel.exists) {
                                binding.abhaNumInputLayout.helperText =
                                    getString(R.string.ABHA_IN_USE)
                            } else {
                                showAbhaAddressAvailableDialog()
//                                navigateToAadhaarOtpVerificationScreen(binding.abhaNumberEt.text.toString())
                            }
                        }

                        is GenerateAbhaUiState.Error -> {
                            binding.checkABHA.isEnabled = true
                            binding.abhaNumberEt.isEnabled = true
                            binding.skipCheckABHA.isEnabled = true
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

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AbdmActivity).setToolbarTitle(R.string.tnc)
        binding.clickHandler = this
        binding.tnc.movementMethod = ScrollingMovementMethod()

        binding.abhaNumberEt.addTextChangedListener {
            val abhaString = it.toString()
            binding.checkABHA.isEnabled = !(TextUtils.isEmpty(abhaString))
            binding.abhaNumInputLayout.helperText = ""
        }
        observeUiState()
    }

    private fun navigateToAadhaarOtpVerificationScreen(healthId:String?) {
        arguments?.putString("healthId",if(healthId == null) null else healthId+"@abdm")
        findNavController().navigate(
            R.id.action_disclaimerFragment_to_enterAbhaCreationDetailsFragment, arguments
        )
    }

    private fun showAbhaAddressAvailableDialog(){
        DialogUtility.showDialog(
            requireContext(),
            "\"${binding.abhaNumberEt.text.toString()}@abdm\" address is available.",
            { onAbhaAddressPresent() },
            {  }
        )
    }

    private fun onAbhaAddressPresent(){
        navigateToAadhaarOtpVerificationScreen(binding.abhaNumberEt.text.toString())
    }

    private fun onAbhaAddressNotPresent(){
      // Intentionally blank
    }

}