package org.commcare.dalvik.abha.ui.main.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.commcare.dalvik.abha.R
import org.commcare.dalvik.abha.databinding.LinkContextCareBinding
import org.commcare.dalvik.abha.ui.main.activity.AbdmActivity
import org.commcare.dalvik.abha.ui.main.activity.AbdmResponseCode
import org.commcare.dalvik.abha.viewmodel.CareContextViewModel
import org.commcare.dalvik.abha.viewmodel.GenerateAbhaUiState
import org.commcare.dalvik.domain.model.CCLinkSuccessResponseModel
import org.commcare.dalvik.domain.model.CheckAbhaResponseModel

class CCLinkFragment :
    BaseFragment<LinkContextCareBinding>(LinkContextCareBinding::inflate) {

    val viewModel: CareContextViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.linkInfoTxt.text = resources.getString(R.string.linking_cc)
        binding.linkHipId.text = viewModel.linkCareContextModel.hipId
        binding.clickHandler = this

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
                            binding.linkInfoTxt.text = resources.getString(R.string.linking_cc_success)

                            val ccLinkSuccessResponseModel =
                                Gson().fromJson(it.data, CCLinkSuccessResponseModel::class.java)
                            binding.linkStatus.text = ccLinkSuccessResponseModel.status
                            binding.linkStatus.setTextColor(
                                ContextCompat.getColor(
                                requireContext(),
                                R.color.green
                            ))
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(false))
                        }

                        is GenerateAbhaUiState.AbdmError -> {
                            binding.linkStatus.text = resources.getString(R.string.cc_not_linked)
                            binding.linkInfoTxt.text = resources.getString(R.string.linking_cc_error)
                            binding.linkStatus.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.red
                                ))
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(false))
                            (activity as AbdmActivity).showBlockerDialog(it.data.getErrorMsg())
                        }

                        is GenerateAbhaUiState.Error -> {
                            binding.linkStatus.setTextColor(
                                ContextCompat.getColor(
                                    requireContext(),
                                    R.color.red
                                ))
                            binding.linkStatus.text = resources.getString(R.string.cc_not_linked)
                            binding.linkInfoTxt.text = resources.getString(R.string.linking_cc_error)
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
        when(view?.id){
            R.id.returnFromCCLink ->{
                dispatchResult()
            }
        }
    }

    private fun dispatchResult() {
        val intent = Intent().apply {
            putExtra("hip_id", viewModel.linkCareContextModel.hipId)
            putExtra("ccLinked", binding.linkStatus.text)
        }

        (activity as AbdmActivity).onContextCareLinkFinished(intent)
    }
}