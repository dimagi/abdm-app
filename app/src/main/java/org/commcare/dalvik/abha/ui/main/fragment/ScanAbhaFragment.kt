package org.commcare.dalvik.abha.ui.main.fragment

import android.os.Bundle
import android.util.Log
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.gson.Gson
import org.commcare.dalvik.abha.R
import org.commcare.dalvik.abha.databinding.EnterAadhaarBinding
import org.commcare.dalvik.abha.databinding.ScanAbhaBinding
import org.commcare.dalvik.abha.ui.main.activity.AbdmActivity
import org.commcare.dalvik.abha.ui.main.activity.ScanAbhaActivity
import org.commcare.dalvik.abha.ui.main.activity.VerificationMode
import org.commcare.dalvik.abha.viewmodel.ScanAbhaViewModel
import org.commcare.dalvik.domain.model.AbhaScanModel
import org.commcare.dalvik.domain.model.AbhaVerificationResultModel
import org.commcare.dalvik.domain.model.HealthCardResponseModel
import org.json.JSONObject
import java.lang.RuntimeException

class ScanAbhaFragment : BaseFragment<ScanAbhaBinding>(ScanAbhaBinding::inflate) {

    private lateinit var codeScanner: CodeScanner
    val viewmodel: ScanAbhaViewModel by activityViewModels()

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.clickHandler = this


        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

//        (activity as ScanAbhaActivity).hideBack()
        initScanner()
    }

    private fun initScanner() {
        codeScanner = CodeScanner(requireContext(), binding.scannerView).apply {
            camera = CodeScanner.CAMERA_BACK
            formats = CodeScanner.ALL_FORMATS
            autoFocusMode = AutoFocusMode.CONTINUOUS
            scanMode = ScanMode.SINGLE
            isAutoFocusEnabled = true
            isFlashEnabled = false
            decodeCallback = DecodeCallback {
                releaseScanner()
                validateScannedData(it.text)
            }
            errorCallback = ErrorCallback {
                Log.d("scan", "===> ${it.message}")
                releaseScanner()
            }
        }
    }


    override fun onClick(view: View?) {
        super.onClick(view)

        view?.let {
            when (it.id) {
                R.id.cancelScan -> {
                    releaseScanner()
                }

                R.id.startScan -> {
                    startScanner()
                }
            }
        }
    }

    override fun onPause() {
        releaseScanner()
        super.onPause()
    }

    private fun startScanner() {
        binding.scanViewHolder.visibility = View.VISIBLE
        codeScanner?.startPreview()
    }

    private fun releaseScanner() {
        activity?.runOnUiThread{
            binding.scanViewHolder.visibility = View.GONE
        }
        codeScanner?.stopPreview()
        codeScanner?.releaseResources()
    }

    private fun validateScannedData(abhaData: String?) {
        abhaData?.let {
            Log.d("scan", "===> ${abhaData}")

//            val data =
//                "{\"hidn\":\"91-7662-6160-6756\",\"hid\":\"91766261606756@sbx\",\"name\":\"Harish Rawat\",\"gender\":\"M\",\"statelgd\":\"7\",\"distlgd\":\"-\",\"dob\":\"23/5/1984\",\"state name\":\"DELHI\",\"district_name\":\"North West Delhi\",\"mobile\":\"9560833229\",\"address\":\"C/O Vijay Singh Rawat D - 17/355 NEAR JIMS COLLAGE SECTOR 3 Rohini\"}"

            try {
                val json = JSONObject(it)

                val isAbhaFormatData = json.has("hidn") && json.has("hid")

                if (!isAbhaFormatData) {
                    throw RuntimeException("Not abha data")
                } else {
                    val abhaScannedModel = Gson().fromJson(it, AbhaScanModel::class.java)
                    activity?.runOnUiThread {
                        viewmodel.abhaScanModel.value = abhaScannedModel
                        navigateToAbhaScanResultScreen()
                        Log.d("scan", "===> ${abhaScannedModel}")
                    }

                }
            } catch (t: Throwable) {
                Log.d("scan", "===> INVALID JSON")
            }
        }

    }

    private fun navigateToAbhaScanResultScreen() {
        activity?.runOnUiThread {
            findNavController().navigate(
                R.id.action_scanAbhaFragment_to_scanAbhaResultFragment
            )
        }
    }

}