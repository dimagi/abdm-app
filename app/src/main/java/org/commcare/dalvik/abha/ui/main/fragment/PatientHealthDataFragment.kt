package org.commcare.dalvik.abha.ui.main.fragment

import android.os.Bundle
import android.view.View
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.commcare.dalvik.abha.databinding.PatientHealthDataBinding
import org.commcare.dalvik.abha.ui.main.adapters.HealthDataAdapter
import org.commcare.dalvik.abha.viewmodel.GenerateAbhaUiState
import org.commcare.dalvik.abha.viewmodel.PatientViewModel
import org.commcare.dalvik.domain.model.HealthContentModel
import org.commcare.dalvik.domain.model.PatientHealthDataModel
import timber.log.Timber

class PatientHealthDataFragment : BaseFragment<PatientHealthDataBinding>(PatientHealthDataBinding::inflate)  {

    val viewModel: PatientViewModel by activityViewModels()
    lateinit var healthDataAdapter:HealthDataAdapter
    val healthDataList = mutableListOf<HealthContentModel>()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getString("artefactId")?.let {artefactId ->

            healthDataAdapter = HealthDataAdapter(healthDataList)
            binding.patientHealthDataList.adapter = healthDataAdapter

            observeUiState()
            fetchHealthData(artefactId)
        }

    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    when (it) {

                        is GenerateAbhaUiState.PatientHealthDataRequested -> {
                            Timber.d("Patient health data requested")
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
                                Timber.d("ALL ARTEFACTS FETCHED")
                                viewModel.uiState.emit(GenerateAbhaUiState.InvalidState)
                                viewModel.uiState.emit(GenerateAbhaUiState.Loading(false))
                            }

                        }

                        is GenerateAbhaUiState.Error -> {
                            Timber.d("ERROR  ARTEFACTS FETCHED")
                            viewModel.uiState.emit(GenerateAbhaUiState.InvalidState)
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