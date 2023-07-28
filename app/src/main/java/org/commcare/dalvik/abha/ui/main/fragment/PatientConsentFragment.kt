package org.commcare.dalvik.abha.ui.main.fragment

import android.graphics.Color
import android.os.Bundle
import android.util.Log
import android.view.View
import android.widget.ArrayAdapter
import android.widget.AutoCompleteTextView
import android.widget.Toast
import com.google.android.material.chip.Chip
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.CompositeDateValidator
import com.google.android.material.datepicker.DateValidatorPointBackward
import com.google.android.material.datepicker.DateValidatorPointForward
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import org.commcare.dalvik.abha.R
import org.commcare.dalvik.abha.databinding.PatientConsentBinding
import org.commcare.dalvik.abha.model.ConsentPermission
import org.commcare.dalvik.abha.model.PatientConsentModel
import org.commcare.dalvik.abha.model.Purpose
import org.commcare.dalvik.abha.utility.CommonUtil
import java.util.Calendar
import kotlin.time.Duration.Companion.days
import kotlin.time.DurationUnit


class PatientConsentFragment : BaseFragment<PatientConsentBinding>(PatientConsentBinding::inflate) {

    private val selectedHiType = mutableListOf<HITYPES>()
    lateinit var timechip: Chip
    private val patientConsentModel = PatientConsentModel(Purpose(PURPOSE.CAREMGT.displayValue))



    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        binding.clickHandler = this
        initConsentPurpose()
        binding.addHiType.setOnClickListener {
            openHiTypeDialog()
        }
    }

    private fun initConsentPurpose() {
        val items = mutableListOf<String>()
        enumValues<PURPOSE>().forEach {
            items.add(it.displayValue)
        }

        val adapter = ArrayAdapter(requireContext(), R.layout.abha_dropdown_row_item, items)

        (binding.consentPurposeCode as? AutoCompleteTextView)?.apply {
            setAdapter(adapter)
            setText(adapter.getItem(0).toString(), false);
        }

    }

    private fun renderSelectedHiTypes() {
        binding.chipGroup.removeAllViews()
        selectedHiType.forEach {
            val chip = Chip(requireContext(),null,R.style.HiTypeStyle)
            chip.text = it.displayValue
            chip.setTextColor(Color.BLACK)
            binding.chipGroup.addView(chip)
        }
    }

    private fun openHiTypeDialog() {
        val items = mutableListOf<String>()
        val checkedStatus = mutableListOf<Boolean>()
        enumValues<HITYPES>().forEach {
            items.add(it.displayValue)
            checkedStatus.add(selectedHiType.contains(it))
        }

        val hiTypesItems = items.toTypedArray()


        val checkedItems = checkedStatus.toBooleanArray()
//            booleanArrayOf(false, false, false, false, false, false, false)


        MaterialAlertDialogBuilder(requireContext())
            .setTitle(resources.getString(R.string.hiTypes))
            .setNeutralButton(resources.getString(R.string.cancel)) { dialog, which ->
                renderSelectedHiTypes()
            }
            .setPositiveButton(resources.getString(R.string.ok)) { dialog, which ->
                renderSelectedHiTypes()
            }
            .setMultiChoiceItems(hiTypesItems, checkedItems) { dialog, which, checked ->
                val selectedType = hiTypesItems[which]
                enumValues<HITYPES>().forEach {
                    if (selectedType.equals(it.displayValue, false)) {
                        if (checked) {
                            selectedHiType.add(it)
                        } else {
                            selectedHiType.remove(it)
                        }
                    }
                }

                renderSelectedHiTypes()
            }
            .show()
    }

    override fun onClick(view: View?) {
        super.onClick(view)
        view?.id?.let {


            when (it) {
                R.id.startDate -> {
                    timechip = binding.startDateChip
                    val dateValidator =
                        DateValidatorPointBackward.before(
                            Calendar.getInstance().timeInMillis - 1.days.toLong(DurationUnit.MILLISECONDS)
                        )

                    captureDateAndTime(
                        resources.getString(R.string.addStartDate),
                        MaterialDatePicker.todayInUtcMilliseconds() -1.days.toLong(DurationUnit.MILLISECONDS),
                        dateValidator
                    )
                }

                R.id.endDate -> {
                    timechip = binding.endDateChip
                    val dateValidatorStart =
                        DateValidatorPointForward.from(
                            patientConsentModel.getPermissionStartDate()
                        )

                    val dateValidatorEnd =
                        DateValidatorPointBackward.before(
                            Calendar.getInstance().timeInMillis - 1.days.toLong(DurationUnit.MILLISECONDS)
                        )

                    val validatorList = mutableListOf<CalendarConstraints.DateValidator>()
                    validatorList.add(dateValidatorStart)
                    validatorList.add(dateValidatorEnd)

                    val compositeDateValidator = CompositeDateValidator.allOf(validatorList)
                    captureDateAndTime(
                        resources.getString(R.string.addEndDate),
                        MaterialDatePicker.todayInUtcMilliseconds(),
                        compositeDateValidator
                    )
                }

                R.id.expiryDate -> {
                    timechip = binding.eraseDateChip
                    val dateValidator =
                        DateValidatorPointForward.from(
                            Calendar.getInstance().timeInMillis
                        )
                    captureDateAndTime(
                        resources.getString(R.string.expiryDate),
                        MaterialDatePicker.todayInUtcMilliseconds(),
                        dateValidator
                    )
                }
            }
        }
    }

    private fun captureDateAndTime(
        title: String,
        selectedTime: Long,
        dateValidator: CalendarConstraints.DateValidator? = null
    ) {
        captureDate(title, selectedTime, ::onDateSelected, dateValidator)
    }

    private fun onDateSelected(selectedDate: Long?) {
        selectedDate?.let {
            captureTime(it)
        } ?: run {
            Toast.makeText(requireContext(), "Date not selected.", Toast.LENGTH_SHORT).show()
        }

    }

    private fun captureTime(selectedDate: Long) {

        val timePicker =
            MaterialTimePicker.Builder()
                .setTimeFormat(TimeFormat.CLOCK_12H)
                .setHour(12)
                .setMinute(10)
                .setTitleText(resources.getString(R.string.select_time))
                .build()

        timePicker.show(parentFragmentManager, timePicker.tag)

        timePicker.addOnPositiveButtonClickListener {
            val minutes = timePicker.minute * 60 * 1000
            val hours = timePicker.hour * 60 * 60 * 1000

            val finalTime = selectedDate + hours + minutes

            Log.d("", "time = ${finalTime}")
            Log.d(
                "",
                "Date is : ${it} --- ${CommonUtil.getFormattedDateTime(finalTime,DATE_FORMAT.SERVER.format)}"
            )
            timechip.text = CommonUtil.getFormattedDateTime(finalTime,DATE_FORMAT.USER.format)

            when (timechip.id) {
                R.id.startDateChip -> {
                    val consentPermission = ConsentPermission(ACCESS_MODE.VIEW.value)
                    patientConsentModel.permission = consentPermission
                    patientConsentModel.setPermissionStartDate(finalTime)
                }

                R.id.endDateChip -> {
                    patientConsentModel.setPermissionEndDate(finalTime)
                }

                R.id.eraseDateChip -> {
                    patientConsentModel.setPermissionExpiryDate(finalTime)
                }
            }
            // call back code
        }
        timePicker.addOnNegativeButtonClickListener {
            Log.d("", "")
            // call back code
        }
        timePicker.addOnCancelListener {
            Log.d("", "")
            // call back code
        }
        timePicker.addOnDismissListener {
            Log.d("", "")
            // call back code
        }

    }

    private fun captureDate(
        title: String,
        selectedDate: Long,
        callback: (selectedDate: Long?) -> Unit,
        dateValidator: CalendarConstraints.DateValidator?
    ) {

        val builder =
            MaterialDatePicker.Builder.datePicker()
                .setTitleText(title)
                .setSelection(selectedDate)

        val datePicker = dateValidator?.let {
            val constraints: CalendarConstraints =
                CalendarConstraints.Builder()
                    .setValidator(it)
                    .build()
            builder.setCalendarConstraints(constraints).build()
        } ?: run {
            builder.build()
        }


        datePicker.addOnCancelListener {
            callback(null)
        }

        datePicker.addOnNegativeButtonClickListener {
            callback(null)
        }

        datePicker.addOnPositiveButtonClickListener {
            callback(it)
        }

        datePicker.show(parentFragmentManager, datePicker.toString())

    }


    enum class PURPOSE(val displayValue: String) {
        CAREMGT("Care Management"),
        PUBHLTH("Public Health"),
        BTG("Break the glass"),
        HPAYMT("Healthcare Payment"),
        DSRCH("Disease Specific Healthcare research")
    }

    enum class HITYPES(val displayValue: String) {
        PRESCRIPTION("Prescription"),
        DIAGNOSTICREPORT("Diagnostic Report"),
        OPCONSULTATION("OP Consultation"),
        DISCHARGESUMMARY("Discharge Summary"),
        IMMUNIZATIONRECORD("Immunization Record"),
        HEALTHDOCUMENTRECORD("Record Artifact"),
        WELLNESSRECORD("Wellness Record")
    }

    enum class ACCESS_MODE(val value: String) {
        VIEW("VIEW")
    }

    enum class DATE_FORMAT(val format:String){
        SERVER("yyyy-MM-dd'T'HH:mm:ss.SSS'Z'"),
        USER("dd MMM YYYY , hh:mm a")
    }
}