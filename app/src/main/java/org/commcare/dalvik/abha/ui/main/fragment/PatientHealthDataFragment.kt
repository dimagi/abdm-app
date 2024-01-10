package org.commcare.dalvik.abha.ui.main.fragment

import android.os.Bundle
import android.view.View
import android.widget.Toast
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import com.google.gson.Gson
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import kotlinx.coroutines.withContext
import org.commcare.dalvik.abha.R
import org.commcare.dalvik.abha.databinding.PatientHealthDataBinding
import org.commcare.dalvik.abha.ui.main.activity.AbdmActivity
import org.commcare.dalvik.abha.ui.main.adapters.FileData
import org.commcare.dalvik.abha.ui.main.adapters.FileType
import org.commcare.dalvik.abha.ui.main.adapters.HealthDataAdapter
import org.commcare.dalvik.abha.utility.DialogType
import org.commcare.dalvik.abha.utility.DialogUtility
import org.commcare.dalvik.abha.viewmodel.GenerateAbhaUiState
import org.commcare.dalvik.abha.viewmodel.PatientViewModel
import org.commcare.dalvik.domain.model.HealthContentModel
import org.commcare.dalvik.domain.model.PatientHealthDataModel
import timber.log.Timber

class PatientHealthDataFragment :
    BaseFragment<PatientHealthDataBinding>(PatientHealthDataBinding::inflate) {

    val viewModel: PatientViewModel by activityViewModels()
    lateinit var healthDataAdapter: HealthDataAdapter
    val healthDataList = mutableListOf<HealthContentModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getString("artefactId")?.let { artefactId ->

            healthDataAdapter = HealthDataAdapter(healthDataList, this::launchImgAndPdfFragment)
            binding.patientHealthDataList.adapter = healthDataAdapter

            observeUiState()
            fetchHealthData(artefactId)
        }

    }

    private fun launchImgAndPdfFragment(fileData: FileData) {
        if (fileData.fileType == FileType.INVALID) {
            Toast.makeText(context, "Invalid file type.", Toast.LENGTH_LONG).show()
        }
        val dialogFragment = AbdmImgAndPdfViewer(fileData)
        dialogFragment.show(parentFragmentManager, "healthData")
    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    when (it) {

                        is GenerateAbhaUiState.PatientHealthDataRequested -> {
                            viewModel.uiState.emit(GenerateAbhaUiState.InvalidState)
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(true))
                        }


                        is GenerateAbhaUiState.Success -> {
                            val healthDataModel =
                                Gson().fromJson(it.data, PatientHealthDataModel::class.java)

                            healthDataList.addAll(healthDataModel.results)
                            healthDataAdapter.notifyDataSetChanged()
                            viewModel.patientHealthData.second.addAll(healthDataModel.results)

                            healthDataModel.next?.let {
                                fetchHealthData(
                                    viewModel.patientHealthData.first,
                                    healthDataModel.transactionId,
                                    healthDataModel.page + 1
                                )
                            } ?: run {
                                viewModel.uiState.emit(GenerateAbhaUiState.Loading(false))
                                if(healthDataModel.results.isEmpty()){
                                        DialogUtility.showDialog(
                                            activity as AbdmActivity,
                                            resources.getString(R.string.no_health_data_available),
                                            { findNavController().popBackStack()},
                                            DialogType.General
                                        )
                                }
                            }
                        }

                        is GenerateAbhaUiState.Error -> {
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(false))
                            (activity as AbdmActivity).showBlockerDialog(it.data.get("message").asString)
                        }

                        is GenerateAbhaUiState.AbdmError -> {
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(false))
                            (activity as AbdmActivity).showBlockerDialog(it.data.message)
                        }

                        else -> {
                            //exhaustive block
                        }
                    }
                }
            }
        }
    }

    private fun fetchHealthData(
        artefactId: String,
        transactionId: String? = null,
        page: Int? = null
    ) {
        if (transactionId == null && page == null) {
            viewModel.patientHealthData = Pair(artefactId, mutableListOf())
        }
        viewModel.fetchPatientHealthData(artefactId, transactionId, page)
    }

}