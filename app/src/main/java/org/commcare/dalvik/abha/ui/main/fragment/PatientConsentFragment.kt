package org.commcare.dalvik.abha.ui.main.fragment

import android.os.Bundle
import android.view.Menu
import android.view.MenuInflater
import android.view.MenuItem
import android.view.View
import android.widget.Toast
import androidx.core.content.ContextCompat
import androidx.core.graphics.drawable.DrawableCompat
import androidx.core.view.MenuHost
import androidx.core.view.MenuProvider
import androidx.fragment.app.activityViewModels
import androidx.lifecycle.Lifecycle
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.MaterialDatePicker
import org.commcare.dalvik.abha.R
import org.commcare.dalvik.abha.databinding.PatientConsentBinding
import org.commcare.dalvik.abha.model.FilterModel
import org.commcare.dalvik.abha.ui.main.adapters.PatientConsentAdapter
import org.commcare.dalvik.abha.utility.CommonUtil
import org.commcare.dalvik.abha.viewmodel.PatientViewModel
import org.commcare.dalvik.domain.model.DATE_FORMAT
import java.util.Calendar
import kotlin.time.Duration.Companion.days
import kotlin.time.DurationUnit


class PatientConsentFragment : BaseFragment<PatientConsentBinding>(PatientConsentBinding::inflate) {

    val viewModel: PatientViewModel by activityViewModels()
    lateinit var consentAdapter: PatientConsentAdapter
    lateinit var filterMenuItem: MenuItem

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)

        viewModel.filterModel.setValue(FilterModel())

        binding.clickHandler = this
        binding.filterModel = viewModel.filterModel.value

        val menuHost: MenuHost = requireActivity()

        menuHost.addMenuProvider(object : MenuProvider {
            override fun onCreateMenu(menu: Menu, menuInflater: MenuInflater) {
                // Add menu items here
                menuInflater.inflate(R.menu.patient_menu, menu)
                filterMenuItem = menu.findItem(R.id.filter)
            }

            override fun onMenuItemSelected(menuItem: MenuItem): Boolean {
                // Handle the menu selection
                return when (menuItem.itemId) {
                    R.id.filter -> {
                        updateFilterMenu()

                        true
                    }

                    else -> false
                }
            }
        }, viewLifecycleOwner, Lifecycle.State.RESUMED)

        consentAdapter = PatientConsentAdapter()
        binding.consentList.setHasFixedSize(true)
        binding.consentList.adapter = consentAdapter


        viewModel.fetchPatientConsent().observe(viewLifecycleOwner) {
            consentAdapter.submitData(lifecycle, it)

        }
    }

    private fun updateFilterMenu() {
        binding.filterLayout.filter.apply {
            if (visibility == View.VISIBLE) {
                visibility = View.GONE
                val icon = filterMenuItem.icon
                if (icon != null) {
                    DrawableCompat.setTint(
                        icon,
                        ContextCompat.getColor(requireContext(), R.color.white)
                    )
                }
            } else {
                visibility = View.VISIBLE
                val icon = filterMenuItem.icon
                if (icon != null) {
                    DrawableCompat.setTint(
                        icon,
                        ContextCompat.getColor(requireContext(), R.color.solid_dark_orange)
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
                            Calendar.getInstance().timeInMillis - 1.days.toLong(DurationUnit.MILLISECONDS)
                        )


                    selectDateWithTime(title = resources.getString(R.string.startDate),
                        MaterialDatePicker.todayInUtcMilliseconds() - 1.days.toLong(DurationUnit.MILLISECONDS),
                        ::onFilterDateSelected,
                        dateValidator,
                        R.id.filterStartDate
                    )

                }

                R.id.filterEndDate -> {

                }

                R.id.applyFilter -> {

                }

                R.id.resetFilter -> {
                  binding.filterLayout.filterModel?.clear()
                }

                R.id.closeFilter -> {
                    updateFilterMenu()
                }

                else -> false

            }

        }

    }

    fun onFilterDateSelected(selectedDate:Long? , id:Int){

        Toast.makeText(requireContext(),"Time : ${selectedDate}",Toast.LENGTH_SHORT).show()
        when(id){
            R.id.filterStartDate -> {
                selectedDate?.let {
                    CommonUtil.getFormattedDateTime(selectedDate,DATE_FORMAT.USER.format)?.let {
                        viewModel.filterModel.value?.fromDate = it
                    }
                }


            }
            R.id.filterEndDate -> {
                selectedDate?.let {
                    CommonUtil.getFormattedDateTime(selectedDate,DATE_FORMAT.USER.format)?.let {
                        viewModel.filterModel.value?.toDate = it
                    }
                }
            }
        }
    }

}