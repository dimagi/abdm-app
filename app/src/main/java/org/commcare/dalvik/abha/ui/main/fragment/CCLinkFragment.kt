package org.commcare.dalvik.abha.ui.main.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.commcare.dalvik.abha.R
import org.commcare.dalvik.abha.databinding.LinkContextCareBinding
import org.commcare.dalvik.abha.ui.main.activity.AbdmActivity
import org.commcare.dalvik.abha.viewmodel.CareContextViewModel
import org.commcare.dalvik.abha.viewmodel.GenerateAbhaUiState

class CCLinkFragment :
    BaseFragment<LinkContextCareBinding>(LinkContextCareBinding::inflate) {

    val viewModel: CareContextViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.linkInfoTxt.text = resources.getString(R.string.linking_cc)
        observeUiState()
        linkCC()

    }

    private fun linkCC(){
        arguments?.getString("accessToken")?.let {accessToken ->

            viewModel.linkCareContext(accessToken)

        }
    }

    private fun observeUiState() {
        lifecycleScope.launch(Dispatchers.Main) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    when (it) {
                        is GenerateAbhaUiState.CCLinkRequested -> {

                        }

                        is GenerateAbhaUiState.Success -> {
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(false))
                            binding.retryLink.visibility = View.GONE
                            binding.retryLink.isEnabled = false

                        }

                        is GenerateAbhaUiState.AbdmError -> {
                            binding.retryLink.visibility = View.VISIBLE
                            binding.retryLink.isEnabled = true
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(false))
                            (activity as AbdmActivity).showBlockerDialog(it.data.getErrorMsg())

                        }

                        is GenerateAbhaUiState.Error -> {
                            binding.retryLink.visibility = View.VISIBLE
                            binding.retryLink.isEnabled = true
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
}