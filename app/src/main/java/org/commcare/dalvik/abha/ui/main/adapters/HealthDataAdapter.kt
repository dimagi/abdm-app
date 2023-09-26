package org.commcare.dalvik.abha.ui.main.adapters

import android.graphics.Paint
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import org.commcare.dalvik.abha.R
import org.commcare.dalvik.abha.databinding.HealDataKeyValueBinding
import org.commcare.dalvik.abha.databinding.HealthDataSectionBinding
import org.commcare.dalvik.abha.databinding.KeyValueBinding
import org.commcare.dalvik.abha.databinding.PatientHealthDataBinding
import org.commcare.dalvik.abha.databinding.PatientHealthDataCellBinding
import org.commcare.dalvik.domain.model.HealthContentModel
import org.commcare.dalvik.domain.model.KeyValueModel
import timber.log.Timber

class HealthDataAdapter(
    private var dataList: List<HealthContentModel>,
    val callback: (fileData: FileData) -> Any
) :
    RecyclerView.Adapter<HealthDataAdapter.HealthDataViewHolder>() {

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): HealthDataViewHolder {
        val inflater = LayoutInflater.from(parent.context)
        val binding = PatientHealthDataCellBinding.inflate(inflater, parent, false)
        return HealthDataViewHolder(binding)
    }


    override fun onBindViewHolder(holder: HealthDataViewHolder, position: Int) {
        holder.bindModel(dataList[position])
    }

    override fun getItemCount(): Int {
        return dataList.size
    }

    inner class HealthDataViewHolder(val binding: PatientHealthDataCellBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindModel(model: HealthContentModel) {
            binding.model = model
            binding.sectionHolder.removeAllViews()
            val context = binding.root.context
            model.content.forEach { sectionModel ->
                val sectionBinding =
                    HealthDataSectionBinding.inflate(LayoutInflater.from(context))
                sectionBinding.model = sectionModel
                var skipNextEntry = false

                sectionModel.entries.forEachIndexed { index, sectionEntry ->
                    if (skipNextEntry) {
                        skipNextEntry = false
                        return@forEachIndexed
                    }
                    val kvBinding =
                        HealDataKeyValueBinding.inflate(LayoutInflater.from(context))
                    if (index % 2 == 0) {
                        kvBinding.tableRow.setBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                R.color.white
                            )
                        )
                    } else {
                        kvBinding.tableRow.setBackgroundColor(
                            ContextCompat.getColor(
                                context,
                                R.color.grey_lighter
                            )
                        )
                    }

                    val value = if (sectionModel.resource == "Binary") {
                        var returnText ="Open "
                        kvBinding.vText.apply {
                            setTextColor(
                                ContextCompat.getColor(
                                    context,
                                    R.color.blue
                                )
                            )
                            paintFlags = Paint.UNDERLINE_TEXT_FLAG
                            skipNextEntry = true



                            val fileType = when (sectionModel.entries[index + 1].value) {
                                "application/pdf" -> {
                                    returnText = "Open PDF"
                                    FileType.PDF
                                }
                                "image/jpeg" -> {
                                    returnText = "Open Image"
                                    FileType.IMAGE
                                }
                                else -> {
                                    FileType.INVALID
                                }
                            }

                            tag = FileData(fileType, sectionEntry.value)

                            setOnClickListener {
                                val fileData = tag as FileData
                                fileData?.let {
                                    callback.invoke(it)
                                }

                            }
                        }
                        returnText
                    } else {
                        skipNextEntry = false
                        sectionEntry.value
                    }

                    kvBinding.model = KeyValueModel(sectionEntry.label, value)

                    sectionBinding.sectionEntryHolder.addView(kvBinding.root)
                }

                binding.sectionHolder.addView(sectionBinding.root)
                binding.expanderView.setOnClickListener {
                    if (binding.sectionHolder.visibility == View.GONE) {
                        binding.sectionHolder.visibility = View.VISIBLE
                        TransitionManager.beginDelayedTransition(
                            binding.sectionHolder,
                            AutoTransition()
                        )
                        binding.expanderView.setImageResource(R.drawable.baseline_expand_less_24)

                    } else {
                        TransitionManager.beginDelayedTransition(
                            binding.sectionHolder,
                            AutoTransition()
                        )
                        binding.sectionHolder.visibility = View.GONE
                        binding.expanderView.setImageResource(R.drawable.baseline_expand_more_24)
                    }
                }

            }
        }
    }
}

data class FileData(val fileType: FileType, val fileData: String)

enum class FileType {
    IMAGE, PDF, INVALID
}

