package org.commcare.dalvik.abha.utility

import android.app.Activity
import android.content.Context
import android.graphics.Typeface
import android.text.Editable
import android.text.TextWatcher
import android.view.View
import android.view.inputmethod.InputMethodManager
import android.widget.TextView
import androidx.appcompat.widget.Toolbar
import androidx.fragment.app.Fragment
import com.google.android.material.textfield.TextInputEditText
import kotlinx.coroutines.channels.awaitClose
import kotlinx.coroutines.flow.callbackFlow


fun TextInputEditText.checkMobileFirstNumber():Boolean{
    if(text?.isEmpty() == true){
        return true
    }else{
        text?.first().toString().toInt().apply {
            return this in IntRange(6, 9)
        }
    }
}

fun Toolbar.changeToolbarFont(){
    for (i in 0 until childCount) {
        val view = getChildAt(i)
        if (view is TextView && view.text == title) {
            view.typeface = Typeface.createFromAsset(view.context.assets, "fonts/nunitosans_bold")
            break
        }
    }
}


/**
 * Extension to observe text
 */
fun TextInputEditText.observeText() =
    callbackFlow {
        val callback = object : TextWatcher {
            override fun beforeTextChanged(p0: CharSequence?, p1: Int, p2: Int, p3: Int) {
            }

            override fun onTextChanged(chSeq: CharSequence?, p1: Int, p2: Int, p3: Int) {
                if(chSeq.isNullOrEmpty()){
                    trySend(0)
                }else{
                    trySend(chSeq.length)
                }
            }

            override fun afterTextChanged(p0: Editable?) {
            }

        }

        this@observeText.addTextChangedListener(callback)

        awaitClose {

        }
    }

fun Context.hideKeyboard(view: View) {
    val inputMethodManager = getSystemService(Activity.INPUT_METHOD_SERVICE) as InputMethodManager
    inputMethodManager.hideSoftInputFromWindow(view.windowToken, 0)
}

fun Fragment.hideKeyboard() {
    view?.let { activity?.hideKeyboard(it) }
}

fun Activity.hideKeyboard() {
    hideKeyboard(currentFocus ?: View(this))
}