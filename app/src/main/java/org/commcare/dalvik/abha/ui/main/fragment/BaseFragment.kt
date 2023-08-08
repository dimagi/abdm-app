package org.commcare.dalvik.abha.ui.main.fragment

import android.os.Bundle
import android.view.LayoutInflater
import android.view.View
import android.view.ViewGroup
import android.widget.Toast
import androidx.fragment.app.Fragment
import androidx.viewbinding.ViewBinding
import com.google.android.material.datepicker.CalendarConstraints
import com.google.android.material.datepicker.MaterialDatePicker
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.timepicker.MaterialTimePicker
import com.google.android.material.timepicker.TimeFormat
import kotlinx.coroutines.flow.callbackFlow
import org.commcare.dalvik.abha.R
import org.commcare.dalvik.abha.ui.main.activity.AbdmActivity
import org.commcare.dalvik.abha.utility.CommonUtil
import org.commcare.dalvik.abha.utility.NetworkHelper
import org.commcare.dalvik.domain.model.DATE_FORMAT
import org.commcare.dalvik.domain.model.TranslationKey
import timber.log.Timber

abstract class BaseFragment<B : ViewBinding>(val bindingInflater: (layoutInflater: LayoutInflater) -> B) :
    Fragment(), View.OnClickListener {

    var mViewDatabinding: B? = null

    val binding: B
        get() = mViewDatabinding as B


    override fun onCreateView(
        inflater: LayoutInflater,
        container: ViewGroup?,
        savedInstanceState: Bundle?
    ): View? {
        mViewDatabinding = bindingInflater.invoke(inflater)

        if (mViewDatabinding == null) {
            throw IllegalAccessException()
        }
        return binding.root
    }

    override fun onClick(view: View?) {

    }

    fun hasNetworkConnectivity(): Boolean {
        val hasConnection = NetworkHelper.isNetworkAvailable(requireActivity())

        if (!hasConnection) {
            (activity as AbdmActivity).showMessageAndDispatchResult(TranslationKey.NO_INTERNET.toString())
        }
        return hasConnection
    }


    fun selectDateWithTime(
        title: String,
        selectedDate: Long,
        callback: (selectedDate: Long?,id:Int) -> Unit,
        dateValidator: CalendarConstraints.DateValidator?,
        id:Int
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
            Toast.makeText(
                requireContext(),
                resources.getString(R.string.dateSelectionFailed),
                Toast.LENGTH_SHORT
            ).show()
        }

        datePicker.addOnNegativeButtonClickListener {
            Toast.makeText(
                requireContext(),
                resources.getString(R.string.dateSelectionFailed),
                Toast.LENGTH_SHORT
            ).show()
        }

        datePicker.addOnPositiveButtonClickListener {
            captureTime(it,callback,id)
        }

        datePicker.show(parentFragmentManager, datePicker.toString())

    }


    private fun captureTime(selectedDate: Long,callback: (selectedDate: Long?,id:Int) -> Unit,id:Int) {

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
            callback(finalTime,id)

            // call back code
        }
        timePicker.addOnNegativeButtonClickListener {
            callback(selectedDate,id)
        }
        timePicker.addOnCancelListener {
            callback(selectedDate,id)
        }
        timePicker.addOnDismissListener {
            callback(selectedDate,id)
        }

    }


}