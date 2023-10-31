package org.commcare.dalvik.abha.ui.main.fragment

import android.os.Bundle
import android.view.View
import android.widget.AdapterView
import android.widget.ArrayAdapter
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.android.material.textfield.MaterialAutoCompleteTextView
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.commcare.dalvik.abha.R
import org.commcare.dalvik.abha.databinding.CCAuthModeBinding
import org.commcare.dalvik.abha.model.LinkCareContextModel
import org.commcare.dalvik.abha.ui.main.activity.AbdmActivity
import org.commcare.dalvik.abha.utility.CommonUtil
import org.commcare.dalvik.abha.viewmodel.CareContextViewModel
import org.commcare.dalvik.abha.viewmodel.GenerateAbhaUiState
import org.commcare.dalvik.domain.model.CCPatientDetails
import org.commcare.dalvik.domain.model.DATE_FORMAT
import timber.log.Timber

class CCFetchAuthModeFragment : BaseFragment<CCAuthModeBinding>(CCAuthModeBinding::inflate),
    AdapterView.OnItemClickListener {

    val viewModel: CareContextViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        viewModel.authModesList.clear()
        observeUiState()
        populateIntentData()
        fetchAuthMethods()
    }

    private fun populateIntentData() {
        arguments?.let {
            val linkCareContextModel = LinkCareContextModel()
            it.getString("abhaId")?.let {
                linkCareContextModel.patientAbhaId = it
            }

            it.getString("purpose")?.let {
                linkCareContextModel.purpose = it
            }

            it.getString("hipId")?.let {
                linkCareContextModel.hipId = it
            }

            it.getString("patientDetail")?.let {
                linkCareContextModel.patient = Gson().fromJson(
                    it,
                    CCPatientDetails::class.java
                )

                linkCareContextModel.patient.careContexts[0].let { ccDetail ->
                    ccDetail.additionalInfo.record_date?.let { recordDate ->
                        val UTC_DIFF = (30 * 60 * 1000) + (5 * 60 * 60 * 1000)
                        val finalRecordDate = CommonUtil.getTimeInMillis(recordDate) - UTC_DIFF
                        CommonUtil.getFormattedDateTime(finalRecordDate , DATE_FORMAT.SERVER.format)?.let {
                            ccDetail.additionalInfo.record_date = it
                        }
                    }
                }
            }

            viewModel.init(linkCareContextModel)
            binding.clickHandler = this
            binding.reqModel = linkCareContextModel
        }
    }

    private fun fetchAuthMethods() {
        if (hasNetworkConnectivity()) {
            viewModel.fetchCareContextAuthModes()
        }
    }


    private fun setAuthModeAdapter() {
        val adapter =
            ArrayAdapter(
                requireContext(),
                R.layout.dropdown_item,
                viewModel.authModesList
            )
        (binding.ccAuthMode as? MaterialAutoCompleteTextView)?.apply {

            setAdapter(adapter)
            setOnItemClickListener(this@CCFetchAuthModeFragment)
            if (viewModel.authModesList.size == 1) {
                setText(adapter.getItem(0).toString(), false)
                viewModel.selectedAuthMethod = adapter.getItem(0).toString()
                binding.ccGenerateAuthOtp.isEnabled = true
            }
        }
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    when (it) {

                        is GenerateAbhaUiState.Success -> {
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(false))

                            try {
                                it.data.getAsJsonArray("modes")?.forEach { modeName ->
                                    if ("DEMOGRAPHICS" != modeName.asString) {
                                        viewModel.authModesList.add(modeName.asString)
                                    }
                                }
                                viewModel.authModesList.sort()

                                setAuthModeAdapter()

                            } catch (e: Exception) {
                                (activity as AbdmActivity).showMessageAndDispatchResult("No authentication methods received.")
                                Timber.d("Auth methods not received")
                            }

                        }

                        is GenerateAbhaUiState.AbdmError -> {
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(false))
                            (activity as AbdmActivity).showBlockerDialog(it.data.message)

                        }

                        is GenerateAbhaUiState.Error -> {
                            (activity as AbdmActivity).showBlockerDialog(it.data.get("message").asString)
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

    override fun onItemClick(parent: AdapterView<*>?, view: View?, position: Int, id: Long) {
        parent?.getItemAtPosition(position).toString().let {
            viewModel.selectedAuthMethod = it
            binding.ccGenerateAuthOtp.isEnabled = true
        }
    }

    override fun onClick(view: View?) {
        super.onClick(view)
        when (view?.id) {
            R.id.ccGenerateAuthOtp -> {
                findNavController().navigate(
                    R.id.action_CCFetchAuthModeFragment_to_verifyCCLinkOtpFragment
                )
            }
        }
    }
}