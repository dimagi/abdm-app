package org.commcare.dalvik.abha.ui.main.adapters

import android.graphics.Color
import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.paging.PagingDataAdapter
import androidx.recyclerview.widget.DiffUtil
import androidx.recyclerview.widget.RecyclerView
import com.google.android.material.chip.Chip
import org.commcare.dalvik.abha.R
import org.commcare.dalvik.abha.databinding.ConsentArtefactCellBinding
import org.commcare.dalvik.domain.model.ConsentArtefactModel

class ConsentArtefactAdapter :
    PagingDataAdapter<ConsentArtefactModel, ConsentArtefactAdapter.ConsentArtefactViewHolder>(
        COMPARATOR
    ) {

    companion object {
        private val COMPARATOR = object : DiffUtil.ItemCallback<ConsentArtefactModel>() {
            override fun areItemsTheSame(
                oldItem: ConsentArtefactModel,
                newItem: ConsentArtefactModel
            ): Boolean {
                return oldItem.id == newItem.id
            }

            override fun areContentsTheSame(
                oldItem: ConsentArtefactModel,
                newItem: ConsentArtefactModel
            ): Boolean {
                return oldItem == newItem
            }

        }
    }


    override fun onBindViewHolder(holder: ConsentArtefactViewHolder, position: Int) {
        val item = getItem(position)
        if (item != null) {
            holder.bindModel(item)
            holder.binding.consentArtefactHolder.setOnClickListener {

            }
        }
    }

    override fun onCreateViewHolder(parent: ViewGroup, viewType: Int): ConsentArtefactViewHolder {
        val binding = ConsentArtefactCellBinding.inflate(
            LayoutInflater.from(parent.context),
            parent,
            false
        )
        return ConsentArtefactViewHolder(binding)
    }


    inner class ConsentArtefactViewHolder(val binding: ConsentArtefactCellBinding) :
        RecyclerView.ViewHolder(binding.root) {

        fun bindModel(model: ConsentArtefactModel) {
            binding.model = model
            binding.careContextChipGroup.removeAllViews()
            model.details.careContexts.forEach { careContextsList ->
                careContextsList.forEach {
                    val chip = Chip(binding.root.context, null, R.style.HiTypeStyle)
                    chip.text = it.careContextReference
                    chip.setTextColor(Color.BLACK)
                    binding.careContextChipGroup.addView(chip)
                }
            }

        }

    }

}