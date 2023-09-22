package org.commcare.dalvik.abha.ui.main.adapters

import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import androidx.core.content.ContextCompat
import androidx.recyclerview.widget.RecyclerView
import androidx.transition.AutoTransition
import androidx.transition.TransitionManager
import org.commcare.dalvik.abha.R
import org.commcare.dalvik.abha.databinding.HealthDataSectionBinding
import org.commcare.dalvik.abha.databinding.KeyValueBinding
import org.commcare.dalvik.abha.databinding.PatientHealthDataBinding
import org.commcare.dalvik.abha.databinding.PatientHealthDataCellBinding
import org.commcare.dalvik.domain.model.HealthContentModel
import org.commcare.dalvik.domain.model.KeyValueModel

class HealthDataAdapter(private var dataList: List<HealthContentModel>) :
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
            model.content.forEach { sectionModel ->
                val sectionBinding =
                    HealthDataSectionBinding.inflate(LayoutInflater.from(binding.root.context))
                sectionBinding.model = sectionModel
                sectionModel.entries.forEachIndexed { index, sectionEntry ->
                    val kvBinding =
                        KeyValueBinding.inflate(LayoutInflater.from(binding.root.context))
                    if (index % 2 == 0) {
                        kvBinding.tableRow.setBackgroundColor(
                            ContextCompat.getColor(
                                binding.root.context,
                                R.color.white
                            )
                        )
                    } else {
                        kvBinding.tableRow.setBackgroundColor(
                            ContextCompat.getColor(
                                binding.root.context,
                                R.color.grey_lighter
                            )
                        )
                    }

                    kvBinding.model = KeyValueModel(
                        sectionEntry.label,
                        if (sectionModel.resource == "Binary") "View File" else sectionEntry.value
                    )
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

