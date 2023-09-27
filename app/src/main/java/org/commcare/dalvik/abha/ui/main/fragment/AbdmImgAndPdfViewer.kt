package org.commcare.dalvik.abha.ui.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.fragment.app.DialogFragment
import org.commcare.dalvik.abha.R
import org.commcare.dalvik.abha.databinding.ImgPdfBinding
import org.commcare.dalvik.abha.ui.main.adapters.FileData
import org.commcare.dalvik.abha.ui.main.adapters.FileType
import java.util.Base64

class AbdmImgAndPdfViewer(private val fileData: FileData) : DialogFragment() {

    lateinit var binding: ImgPdfBinding
    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        isCancelable = false
        binding = ImgPdfBinding.inflate(inflater, container, false)
        binding.model = fileData
        return binding.root
    }

    override fun getTheme(): Int {
        return R.style.DialogTheme
    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        if (fileData.fileType == FileType.PDF) {
            val decoded = Base64.getDecoder().decode(fileData.fileData)
            binding.pdfView.fromBytes(decoded).load()

        }

        binding.closeDialog.setOnClickListener {
            dismiss()
        }

    }
}