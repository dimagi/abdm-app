package org.commcare.dalvik.abha.ui.main.custom

import android.content.Context
import android.util.AttributeSet
import android.view.View
import android.widget.TextView
import androidx.constraintlayout.widget.ConstraintLayout
import com.google.android.material.progressindicator.CircularProgressIndicator
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.delay
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.launch
import org.commcare.dalvik.abha.R

class TimeProgressBar @JvmOverloads constructor(
    context: Context, attrs: AttributeSet? = null
) : ConstraintLayout(context, attrs) {

    var counter: Int = 0
    val pb: CircularProgressIndicator by lazy { findViewById(R.id.progressBar) }
    private val counterView: TextView by lazy { findViewById(R.id.progressCount) }
    val timeState:MutableStateFlow<OtpTimerState> = MutableStateFlow(OtpTimerState.None)


    init {
        inflate(getContext(), R.layout.number_progress, this)
        attrs?.let {
            context.obtainStyledAttributes(
                it,
                R.styleable.custom_number_progress_attributes, 0, counter
            ).apply {
                try {
                    pb.max =
                        getInt(R.styleable.custom_number_progress_attributes_maxCounter, 60)
                    pb.progress = pb.max
                } finally {
                    recycle()
                }
            }
        }
    }

    fun startTimer(){
        CoroutineScope(Dispatchers.Main).launch {
            timeState.emit(OtpTimerState.TimerStarted)
            visibility = View.VISIBLE
            counterView.text = pb.progress.toString()
            while (pb.progress > 0){
                delay(1000)
                pb.progress = pb.progress - 1
                counterView.text = pb.progress.toString()
            }
            visibility = View.GONE
            timeState.emit(OtpTimerState.TimerOver)
            pb.progress = pb.max
        }
    }
}

sealed class OtpTimerState {
    object None:OtpTimerState()
    object TimerStarted:OtpTimerState()
    object TimerOver:OtpTimerState()
}
