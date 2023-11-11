package org.commcare.dalvik.abha.ui.main.fragment

import android.content.Intent
import android.os.Bundle
import android.view.View
import android.view.View.OnClickListener
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.commcare.dalvik.abha.R
import org.commcare.dalvik.abha.databinding.NotifyPatientBinding
import org.commcare.dalvik.abha.ui.main.activity.AbdmActivity
import org.commcare.dalvik.abha.ui.main.activity.AbdmResponseCode
import org.commcare.dalvik.abha.viewmodel.AbdmViewModel
import org.commcare.dalvik.abha.viewmodel.GenerateAbhaUiState
import org.commcare.dalvik.domain.model.HealthCardResponseModel
import org.commcare.dalvik.domain.model.HipModel
import org.commcare.dalvik.domain.model.NotifyPatientResponseModel
import org.commcare.dalvik.domain.model.PatientNotificationModel

class NotifyPatientFragment : BaseFragment<NotifyPatientBinding>(NotifyPatientBinding::inflate)  , OnClickListener {

    val viewModel: AbdmViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        try {
            arguments?.getString("phoneNo")?.let {phoneNumber ->
                arguments?.getString("hip")?.let { hipId ->
                    observeUiState()
                    val patientNotificationModel = PatientNotificationModel(phoneNumber , HipModel(hipId))
                    binding.model = patientNotificationModel
                    viewModel.notifyPatient(patientNotificationModel)
                    binding.clickHandler = this
                }
            }
        }catch (e:Exception){
            (activity as AbdmActivity).showBlockerDialog("Invalid intent data.")
        }

    }

    private fun observeUiState() {
        viewLifecycleOwner.lifecycleScope.launch {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewModel.uiState.collect {
                    when (it) {
                        is GenerateAbhaUiState.Success -> {
                           val notifyPatientResponseModel =
                                Gson().fromJson(it.data, NotifyPatientResponseModel::class.java)
                            binding.notifyStatus.text = notifyPatientResponseModel.status.toString().uppercase()
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(false))
                        }

                        is GenerateAbhaUiState.Error -> {
                            binding.notifyStatus.text ="FALSE"
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(false))
                        }

                        is GenerateAbhaUiState.AbdmError -> {
                            viewModel.uiState.emit(GenerateAbhaUiState.Loading(false))
                            binding.notifyStatus.text ="FALSE"
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
            R.id.returnFromNotify ->{
                dispatchResult()
            }
        }

    }

    private fun dispatchResult() {
        val intent = Intent().apply {
            putExtra("status", binding.notifyStatus.text.toString() )
            putExtra("code", AbdmResponseCode.SUCCESS.value)
            putExtra("message", "Notify patient completed.")
        }
        (activity as AbdmActivity).onAbhaScanCompleted(intent)
    }
}