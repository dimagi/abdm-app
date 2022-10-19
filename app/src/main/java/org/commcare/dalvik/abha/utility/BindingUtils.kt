package org.commcare.dalvik.abha.utility

import android.graphics.BitmapFactory
import android.util.Base64
import android.widget.*
import androidx.databinding.BindingAdapter
import com.bumptech.glide.Glide
import com.google.android.material.textfield.TextInputEditText
import com.google.android.material.textfield.TextInputLayout
import org.commcare.dalvik.abha.R
import org.commcare.dalvik.domain.model.LanguageManager
import org.commcare.dalvik.domain.model.TranslationKey
import org.json.JSONException


class BindingUtils {
    companion object {
        @JvmStatic
        @BindingAdapter("loadImage")
        fun loadImage(view: ImageView, src: String?) {
            val decodedString: ByteArray = Base64.decode(src, Base64.DEFAULT)
            val decodedByte = BitmapFactory.decodeByteArray(decodedString, 0, decodedString.size)
            Glide.with(view.context)
                .load(decodedByte)
                .error(R.drawable.ic_baseline_person_24)
                .into(view)
        }

        @JvmStatic
        @BindingAdapter("translatedTextKey")
        fun setTranslatedText(view: Button, key: TranslationKey) {
            try {
                val translatedText = LanguageManager.getTranslatedValue(key)
                view.text = translatedText
            } catch (e: JSONException) {
                view.text = key.name
            }
        }

        @JvmStatic
        @BindingAdapter("translatedTextKey")
        fun setTranslatedText(view: TextInputEditText, key: TranslationKey) {
            try {
                val hintText = LanguageManager.getTranslatedValue(key)
                view.hint = hintText
            } catch (e: JSONException) {
                view.hint = key.name
            }
        }

        @JvmStatic
        @BindingAdapter("translatedTextKey")
        fun setTranslatedText(view: TextInputLayout, key: TranslationKey) {
            try {
                val hintText = LanguageManager.getTranslatedValue(key)
                view.hint = hintText
            } catch (e: JSONException) {
                view.hint = key.name
            }
        }

        @JvmStatic
        @BindingAdapter("translatedTextKey")
        fun setTranslatedText(view: TextView, key: TranslationKey) {
            try {
                val hintText = LanguageManager.getTranslatedValue(key)
                view.text = hintText
            } catch (e: JSONException) {
                view.text = key.name
            }
        }

        @JvmStatic
        @BindingAdapter("translatedTextKey")
        fun setTranslatedText(view: CheckBox, key: TranslationKey) {
            try {
                val text = LanguageManager.getTranslatedValue(key)
                view.text = text
            } catch (e: JSONException) {
                view.text = key.name
            }
        }

        @JvmStatic
        @BindingAdapter("translatedTextKey")
        fun setTranslatedText(view: AutoCompleteTextView, key: TranslationKey) {
            try {
                val hintText = LanguageManager.getTranslatedValue(key)
                view.hint = hintText
            } catch (e: JSONException) {
                view.hint = key.name
            }
        }


    }
}