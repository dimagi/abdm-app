package org.commcare.dalvik.abha.ui.main.fragment

import android.Manifest
import android.content.pm.PackageManager
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.Toast
import androidx.activity.OnBackPressedCallback
import androidx.activity.result.ActivityResultLauncher
import androidx.activity.result.contract.ActivityResultContracts
import androidx.core.app.ActivityCompat
import androidx.core.content.ContextCompat
import androidx.fragment.app.activityViewModels
import androidx.navigation.fragment.findNavController
import com.budiyev.android.codescanner.AutoFocusMode
import com.budiyev.android.codescanner.CodeScanner
import com.budiyev.android.codescanner.DecodeCallback
import com.budiyev.android.codescanner.ErrorCallback
import com.budiyev.android.codescanner.ScanMode
import com.google.gson.Gson
import org.commcare.dalvik.abha.R
import org.commcare.dalvik.abha.databinding.ScanAbhaBinding
import org.commcare.dalvik.abha.ui.main.activity.AbdmActivity
import org.commcare.dalvik.abha.viewmodel.ScanAbhaViewModel
import org.commcare.dalvik.domain.model.AbhaScanModel
import org.json.JSONObject
import timber.log.Timber
import java.lang.RuntimeException

class ScanAbhaFragment : BaseFragment<ScanAbhaBinding>(ScanAbhaBinding::inflate) {

    private lateinit var codeScanner: CodeScanner
    val viewmodel: ScanAbhaViewModel by activityViewModels()
    lateinit var requestPermissionLauncher: ActivityResultLauncher<String>

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        binding.clickHandler = this


        val callback = object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {

            }
        }
        requireActivity().onBackPressedDispatcher.addCallback(viewLifecycleOwner, callback)

        requestPermissionLauncher =
            registerForActivityResult(
                ActivityResultContracts.RequestPermission()
            ) { isGranted: Boolean ->
                if (isGranted) {
                    startScanner()
                } else {
                    Toast.makeText(requireContext(), "Camera permission denied", Toast.LENGTH_LONG)
                        .show()
                }
            }

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
                Timber.d("scan===> ${it.message}")
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
                    beginScan()
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
        codeScanner.startPreview()
    }

    private fun releaseScanner() {
        codeScanner.stopPreview()
        codeScanner.releaseResources()
        activity?.runOnUiThread {
            binding.scanViewHolder.visibility = View.GONE
        }
    }

    private fun validateScannedData(abhaData: String?) {
        abhaData?.let {
            Timber.d("scan ===> ${abhaData}")

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
                        Timber.d("scan ===> ${abhaScannedModel}")
                    }

                }
            } catch (t: Throwable) {
                activity?.runOnUiThread {
                    (activity as AbdmActivity).showBlockerDialog(
                        resources.getText(R.string.scanAbhaErrorMsg).toString()
                    )
                    Timber.d("scan ===> INVALID JSON")
                }
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

    private fun beginScan() {

        if (ContextCompat.checkSelfPermission(
                requireContext(), Manifest.permission.CAMERA) ==
            PackageManager.PERMISSION_GRANTED) {
            startScanner()
        } else if (shouldShowRequestPermissionRationale(Manifest.permission.CAMERA)) {
            Toast.makeText(requireContext(), resources.getString(R.string.permission_camera_rationale_msg), Toast.LENGTH_LONG)
                .show()
        } else {
            requestPermissionLauncher.launch(
                Manifest.permission.CAMERA);
        }

    }

}