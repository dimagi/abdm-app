package org.commcare.dalvik.abha.ui.main.fragment

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.gson.Gson
import kotlinx.coroutines.launch
import org.commcare.dalvik.abha.R
import org.commcare.dalvik.abha.databinding.ConsentArtefactBinding
import org.commcare.dalvik.abha.ui.main.adapters.ConsentArtefactAdapter
import org.commcare.dalvik.abha.ui.main.adapters.ConsentPageLoaderAdapter
import org.commcare.dalvik.abha.viewmodel.GenerateAbhaUiState
import org.commcare.dalvik.abha.viewmodel.PatientViewModel
import org.commcare.dalvik.domain.model.PatientHealthDataModel
import timber.log.Timber

class ConsentArtefactFragment :
    BaseFragment<ConsentArtefactBinding>(ConsentArtefactBinding::inflate) {

    val viewModel: PatientViewModel by activityViewModels()
    lateinit var artefactAdapter: ConsentArtefactAdapter

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
                        artefactAdapter.refresh()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)


        artefactAdapter = ConsentArtefactAdapter(this::navigateToPatientHealthData)

        binding.artefactList.apply {
            setHasFixedSize(true)
            adapter = artefactAdapter.withLoadStateHeaderAndFooter(
                footer = ConsentPageLoaderAdapter(artefactAdapter::retry),
                header = ConsentPageLoaderAdapter(artefactAdapter::retry)
            )
        }

        arguments?.getString("contentRequestId")?.let {
            viewModel.initArtefactFilterModel(it)
        }

        viewModel.fetchConsentArtefacts().observe(viewLifecycleOwner) {
            artefactAdapter.submitData(lifecycle, it)
        }

        //LOAD STATE
        artefactAdapter.addLoadStateListener { loadState ->

            val isLoading = loadState.refresh is LoadState.Loading
            binding.statusLoading.isVisible = isLoading
            if (isLoading) {
                binding.statusView.isVisible = false
            }

            if (loadState.refresh is LoadState.Error) {
                binding.statusView.isVisible = true
                binding.statusView.text = resources.getText(R.string.loadErrorMsg)
            }
            if (loadState.append.endOfPaginationReached) {
                if (artefactAdapter.itemCount < 1) {
                    binding.statusView.isVisible = true
                    binding.statusView.text = resources.getText(R.string.noData)
                } else {
                    binding.statusView.isVisible = false
                }
            }
        }

    }

    private fun navigateToPatientHealthData(artefactId: String) {
        findNavController().navigate(
            R.id.action_consentArtefactFragment_to_patientHealthDataFragment, bundleOf(
                "artefactId" to artefactId
            )
        )

    }
}