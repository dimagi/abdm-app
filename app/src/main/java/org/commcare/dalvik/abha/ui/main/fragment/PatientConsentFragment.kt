package org.commcare.dalvik.abha.ui.main.fragment

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.os.bundleOf
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.core.view.isVisible
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import androidx.navigation.fragment.findNavController
import androidx.paging.LoadState
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import org.commcare.dalvik.abha.R
import org.commcare.dalvik.abha.databinding.PatientConsentBinding
import org.commcare.dalvik.abha.ui.main.activity.AbdmActivity
import org.commcare.dalvik.abha.ui.main.adapters.ConsentPageLoaderAdapter
import org.commcare.dalvik.abha.ui.main.adapters.PatientConsentAdapter
import org.commcare.dalvik.abha.utility.CommonUtil
import org.commcare.dalvik.abha.utility.hideKeyboard
import org.commcare.dalvik.abha.viewmodel.PatientViewModel
import org.commcare.dalvik.data.paging.AbdmException
import org.commcare.dalvik.domain.model.DATE_FORMAT
import org.commcare.dalvik.domain.model.PatientConsentModel
import timber.log.Timber
import java.util.Calendar
import kotlin.time.Duration.Companion.days
import kotlin.time.DurationUnit


class PatientConsentFragment : BaseFragment<PatientConsentBinding>(PatientConsentBinding::inflate) {

    val viewModel: PatientViewModel by activityViewModels()
    lateinit var consentAdapter: PatientConsentAdapter
    lateinit var filterMenuItem: MenuItem

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        arguments?.getString("abha_id")?.let { abhaId ->
            viewModel.initPatientAbhaId(abhaId)
        }

        viewModel.consentFilterModel.observe(viewLifecycleOwner) {
            viewModel.updatePatientFilter()
        }

        binding.clickHandler = this
        binding.filterModel = viewModel.consentFilterModel.value

        val menuHost: MenuHost = requireActivity()

        //MENU
        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.patient_menu, menu)
                filterMenuItem = menu.findItem(R.id.filter)
                filterMenuItem.isVisible = true
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.filter -> {
                        updateFilterMenu()
                        true
                    }

                    R.id.refresh -> {
                        consentAdapter.refresh()
                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        val patientName = arguments?.getString("patient_name")
        consentAdapter = PatientConsentAdapter(patientName, this::onPatientConsentClicked)

        binding.consentList.apply {
            setHasFixedSize(true)
            adapter = consentAdapter.withLoadStateHeaderAndFooter(
                footer = ConsentPageLoaderAdapter(consentAdapter::retry),
                header = ConsentPageLoaderAdapter(consentAdapter::retry)
            )
        }

        binding.filterLayout.apply {
            filterEndDateChip.setOnCloseIconClickListener {
                viewModel.consentFilterModel.value?.toDate = null
            }
            filterStartDateChip.setOnCloseIconClickListener {
                viewModel.consentFilterModel.value?.fromDate = null
            }
        }


        viewModel.fetchPatientConsent().observe(viewLifecycleOwner) {
            consentAdapter.submitData(lifecycle, it)
        }


        //PAGE RETRY
        binding.loadingLayout.retry.setOnClickListener {
            consentAdapter.refresh()
        }

        //LOAD STATE
        consentAdapter.addLoadStateListener { loadState ->

            val isLoading = loadState.refresh is LoadState.Loading
            binding.statusLoading.isVisible = isLoading
            if (isLoading) {
                binding.statusView.isVisible = false
            }

            if (loadState.refresh is LoadState.Error) {
                binding.statusView.isVisible = true

                val abdmException: AbdmException? =
                    (loadState.refresh as LoadState.Error).error as? AbdmException
                abdmException?.let {
                    it.message?.let { msg -> (activity as AbdmActivity).showBlockerDialog(msg) }
                } ?: run {
                    binding.statusView.text = resources.getText(R.string.loadErrorMsg)
                }

            }

            if (loadState.append.endOfPaginationReached) {
                if (consentAdapter.itemCount < 1) {
                    binding.statusView.isVisible = true
                    binding.statusView.text = resources.getText(R.string.noData)
                } else {
                    binding.statusView.isVisible = false
                }

            }

        }

        binding.createConsentBtn.setOnClickListener {
            navigateToCreateConsentScreen()
        }

        binding.backBt.setOnClickListener {
            (activity as AbdmActivity).finish()
        }
    }

    private fun updateFilterMenu() {
        binding.filterLayout.filter.apply {
            if (visibility == View.VISIBLE) {
                visibility = View.GONE
            } else {
                visibility = View.VISIBLE
            }

            binding.filterModel?.let {
                val tintColor =
                    if (it.isFilterApplied()) R.color.solid_dark_orange else R.color.white
                val icon = filterMenuItem.icon
                if (icon != null) {
                    DrawableCompat.setTint(
                        icon,
                        ContextCompat.getColor(requireContext(), tintColor)
                    )
                }
            }

        }
    }

    override fun onClick(view: View?) {
        super.onClick(view)
        view?.id.let {

            when (it) {
                R.id.filterStartDate -> {
                    val dateValidator =
                        DateValidatorPointBackward.before(
                            Calendar.getInstance().timeInMillis
                        )

                    selectDateWithTime(
                        title = resources.getString(R.string.startDate),
                        MaterialDatePicker.todayInUtcMilliseconds() - 1.days.toLong(DurationUnit.MILLISECONDS),
                        ::onFilterDateSelected,
                        dateValidator,
                        R.id.filterStartDate,
                        false
                    )

                }

                R.id.filterEndDate -> {
                    val validatorList = mutableListOf<CalendarConstraints.DateValidator>()

                    val dateValidatorStart =
                        binding.filterModel?.startDateFilterInMS?.let { ms ->
                            DateValidatorPointForward.from(
                                ms
                            )
                        }
                    dateValidatorStart?.let {
                        validatorList.add(dateValidatorStart)
                    }

                    val dateValidatorEnd =
                        DateValidatorPointBackward.before(
                            Calendar.getInstance().timeInMillis
                        )

                    validatorList.add(dateValidatorEnd)

                    val compositeDateValidator = CompositeDateValidator.allOf(validatorList)

                    selectDateWithTime(
                        title = resources.getString(R.string.startDate),
                        MaterialDatePicker.todayInUtcMilliseconds() - 1.days.toLong(DurationUnit.MILLISECONDS),
                        ::onFilterDateSelected,
                        compositeDateValidator,
                        R.id.filterEndDate,
                        false
                    )

                }

                R.id.applyFilter -> {
                    hideKeyboard()
                    updateFilterMenu()
                    consentAdapter.refresh()
                }

                R.id.resetFilter -> {
                    binding.filterModel?.clear()
                    viewModel.updatePatientFilter()
                }

                R.id.closeFilter -> {
                    updateFilterMenu()
                    hideKeyboard()
                }

                else -> false

            }

        }

    }

    private fun onFilterDateSelected(selectedDate: Long?, id: Int) {

        Timber.d("Time : ${selectedDate}")
        when (id) {
            R.id.filterStartDate -> {
                selectedDate?.let {
                    CommonUtil.getFormattedDateTime(selectedDate, DATE_FORMAT.ONLY_DATE.format)
                        ?.let {
                            viewModel.consentFilterModel.value?.fromDate = it
                            viewModel.consentFilterModel.value?.startDateFilterInMS = selectedDate
                        }
                }

            }

            R.id.filterEndDate -> {
                selectedDate?.let {
                    CommonUtil.getFormattedDateTime(selectedDate, DATE_FORMAT.ONLY_DATE.format)
                        ?.let {
                            viewModel.consentFilterModel.value?.toDate = it
                        }
                }
            }
        }
    }

    private fun onPatientConsentClicked(patientConsentModel: PatientConsentModel) {
        Timber.d("Clicked : ${patientConsentModel.id}")
        patientConsentModel.status?.let {
            if (it == "GRANTED") {
                navigateToConsentArtefactScreen(patientConsentModel.consentRequestId)
            } else {
                Toast.makeText(
                    requireContext(),
                    resources.getString(R.string.notGrantedMsg),
                    Toast.LENGTH_SHORT
                ).show()
            }
        }

    }

    private fun navigateToConsentArtefactScreen(consentReqId: String) {
        activity?.runOnUiThread {
            arguments?.putString("contentRequestId", consentReqId)
            findNavController().navigate(
                R.id.action_patientConsentFragment_to_consentArtefactFragment, arguments
            )
        }
    }

    private fun navigateToCreateConsentScreen() {
        activity?.runOnUiThread {
            findNavController().navigate(
                R.id.action_patientConsentFragment_to_createPatientConsentFragment, arguments)
        }
    }

}