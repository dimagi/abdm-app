package org.commcare.dalvik.abha.ui.main.fragment

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.paging.LoadState
import androidx.recyclerview.widget.LinearLayoutManager
import org.commcare.dalvik.abha.R
import org.commcare.dalvik.abha.databinding.ConsentArtefactBinding
import org.commcare.dalvik.abha.ui.main.adapters.ConsentArtefactAdapter
import org.commcare.dalvik.abha.ui.main.adapters.ConsentPageLoaderAdapter
import org.commcare.dalvik.abha.viewmodel.PatientViewModel

class ConsentArtefactFragment : BaseFragment<ConsentArtefactBinding>(ConsentArtefactBinding::inflate) {

    val viewModel: PatientViewModel by activityViewModels()
    lateinit var arefactAdapter: ConsentArtefactAdapter

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        //MENU
        val menuHost: MenuHost = requireActivity()
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.patient_menu, menu)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {

                    R.id.refresh -> {
                        arefactAdapter.refresh()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)


        arefactAdapter = ConsentArtefactAdapter()

        binding.artefactList.apply {
            setHasFixedSize(true)
            adapter = arefactAdapter.withLoadStateHeaderAndFooter(
                footer = ConsentPageLoaderAdapter(arefactAdapter::retry),
                header = ConsentPageLoaderAdapter(arefactAdapter::retry)
            )
        }

        arguments?.getString("contentRequestId")?.let {
            viewModel.initArtefactFilterModel(it)
        }

        viewModel.fetchConsentArtefacts().observe(viewLifecycleOwner) {
            arefactAdapter.submitData(lifecycle, it)
        }

        //LOAD STATE
        arefactAdapter.addLoadStateListener { loadState ->

            val isLoading = loadState.refresh is LoadState.Loading
            binding.statusLoading.isVisible = isLoading
            if(isLoading){
                binding.statusView.isVisible = false
            }

            if (loadState.refresh is LoadState.Error) {
                binding.statusView.isVisible = true
                binding.statusView.text = resources.getText(R.string.loadErrorMsg)
            }
            if (loadState.append.endOfPaginationReached) {
                if (arefactAdapter.itemCount < 1) {
                    binding.statusView.isVisible = true
                    binding.statusView.text = resources.getText(R.string.noData)
                } else {
                    binding.statusView.isVisible = false
                }
            }
        }

    }
}