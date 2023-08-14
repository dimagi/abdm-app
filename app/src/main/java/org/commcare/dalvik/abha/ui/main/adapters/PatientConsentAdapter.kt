package org.commcare.dalvik.abha.ui.main.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import org.commcare.dalvik.abha.R
import org.commcare.dalvik.abha.databinding.PatientConsentCellBinding
import org.commcare.dalvik.abha.utility.CommonUtil
import org.commcare.dalvik.domain.model.DATE_FORMAT
import org.commcare.dalvik.domain.model.PatientConsentModel
import java.text.SimpleDateFormat
import java.util.Date

class PatientConsentAdapter(val callback :(patientConsentModel:PatientConsentModel)->Unit) :
    PagingDataAdapter<PatientConsentModel, PatientConsentAdapter.PatientConsentViewHolder>(
        COMPARATOR
    ) {

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<PatientConsentModel>() {
            override fun areItemsTheSame(
                oldItem: PatientConsentModel,
                newItem: PatientConsentModel
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: PatientConsentModel,
                newItem: PatientConsentModel
            ): Boolean {
                return oldItem == newItem
            }

        }
    }

    override fun onBindViewHolder(holder: PatientConsentViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bindModel(item)
            holder.binding.patientContentHolder.setOnClickListener {
                callback.invoke(item)
            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): PatientConsentViewHolder {
       val binding =  PatientConsentCellBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return PatientConsentViewHolder(binding)
    }


    inner class PatientConsentViewHolder(val binding: PatientConsentCellBinding) : RecyclerView.ViewHolder(binding.root) {

        fun bindModel(model: PatientConsentModel){
            binding.model = model
            renderHealthInfoTypes()
            model.healthInfoFromDate?.let {
                binding.fromDate.text = CommonUtil.getUserFormatDate(it)
            }

            model.healthInfoToDate?.let {
                binding.toDate.text = CommonUtil.getUserFormatDate(it)
            }

            model.expiryDate?.let {
                binding.expiryDate.text = CommonUtil.getUserFormatDate(it)
            }

        }


        private fun renderHealthInfoTypes(){

            binding.healthTypeChipGroup.removeAllViews()
            binding.model?.healthInfoType?.forEach {
                val chip = Chip(binding.root.context, null, R.style.HiTypeStyle)
                chip.text = it
                chip.setTextColor(Color.BLACK)
                binding.healthTypeChipGroup.addView(chip)
            }

        }

    }


}