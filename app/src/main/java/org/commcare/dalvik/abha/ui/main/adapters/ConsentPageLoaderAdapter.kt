package org.commcare.dalvik.abha.ui.main.adapters

import android.view.LayoutInflater
import android.view.ViewGroup
import androidx.core.view.isVisible
import androidx.paging.LoadState
import androidx.paging.LoadStateAdapter
import androidx.recyclerview.widget.RecyclerView
import org.commcare.dalvik.abha.databinding.ConsentLoaderStateBinding

class ConsentPageLoaderAdapter(val retry: () -> Unit ) : LoadStateAdapter<ConsentPageLoaderAdapter.LoaderViewHolder>() {

    class LoaderViewHolder(val binding: ConsentLoaderStateBinding , val retry: () -> Unit) :
        RecyclerView.ViewHolder(binding.root) {
        fun bind(loadState: LoadState) {
            binding.retry.setOnClickListener {
                retry.invoke()
            }

            if (loadState is LoadState.Error) {
                binding.errMsg.text = loadState.error.localizedMessage
            }

            binding.loadingProgress.isVisible = loadState is LoadState.Loading
            binding.retry.isVisible = loadState is LoadState.Error
            binding.errMsg.isVisible = loadState is LoadState.Error

        }
    }

    override fun onBindViewHolder(holder: LoaderViewHolder, loadState: LoadState) {
        holder.bind(loadState)
    }

    override fun onCreateViewHolder(parent: ViewGroup, loadState: LoadState): LoaderViewHolder {

        val binding =
            ConsentLoaderStateBinding.inflate(LayoutInflater.from(parent.context), parent, false)
        return LoaderViewHolder(binding ,retry)
    }

}
