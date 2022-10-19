package org.commcare.dalvik.abha.ui.main.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.fragment.app.activityViewModels
import org.commcare.dalvik.abha.databinding.AbhaVerificationResultBinding
import org.commcare.dalvik.abha.ui.main.activity.AbdmActivity
import org.commcare.dalvik.abha.ui.main.activity.AbdmResponseCode
import org.commcare.dalvik.abha.viewmodel.AbdmViewModel
import org.commcare.dalvik.domain.model.AbhaVerificationResultModel

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
    }

    override fun onClick(view: View?) {
        super.onClick(view)
        dispatchResult()
    }

    private fun dispatchResult() {
        val intent = Intent().apply {
            putExtra("abha_id", binding.model?.healthId)
            putExtra("code", AbdmResponseCode.SUCCESS.value)
            putExtra("verified", binding.model?.status)
            putExtra("message", "ABHA verification completed.")
        }

        (activity as AbdmActivity).onAbhaNumberVerification(intent)
    }
}