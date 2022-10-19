package org.commcare.dalvik.abha.ui.main.fragment

import android.content.Intent
import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import org.commcare.dalvik.abha.R
import org.commcare.dalvik.abha.databinding.AbhaDetailBinding
import org.commcare.dalvik.abha.databinding.KeyValueBinding
import org.commcare.dalvik.abha.ui.main.activity.AbdmActivity
import org.commcare.dalvik.abha.ui.main.activity.AbdmResponseCode
import org.commcare.dalvik.abha.viewmodel.AbdmViewModel

class AbhaCreationResultFragment : BaseFragment<AbhaDetailBinding>(AbhaDetailBinding::inflate) {
    private val viewModel: AbdmViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AbdmActivity).hideMenu()
        binding.clickHandler = this
        binding.model = viewModel.abhaDetailModel.value
        renderAadhaarData()

        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                dispatchResult()
            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        (activity as AbdmActivity).hideBack()

//        viewModel.abhaRequestModel.value?.aadhaar?.let {
//            viewModel.clearOtpRequestState(it)
//        }
    }


    private fun renderAadhaarData() {
        viewModel.abhaDetailModel.value?.getAadhaarDataList()?.forEachIndexed { index, kvModel ->
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
            putExtra("code", AbdmResponseCode.SUCCESS.value)
            putExtra("verified", "true")
            putExtra("message", "ABHA creation completed.")
            if (binding.shareWithCC.isChecked) {
                putExtra("aadhaarData", binding.model?.data?.toString())
            }
        }
        (activity as AbdmActivity).onAbhaNumberReceived(intent)
    }


}