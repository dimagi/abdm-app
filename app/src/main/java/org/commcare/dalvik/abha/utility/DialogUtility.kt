package org.commcare.dalvik.abha.utility

import android.content.Context
import com.google.android.material.dialog.MaterialAlertDialogBuilder
import dagger.hilt.android.qualifiers.ApplicationContext
import org.commcare.dalvik.abha.R
import javax.inject.Inject
import kotlin.reflect.typeOf

object DialogUtility {

    private fun getTheme(type: DialogType) = when (type) {
        DialogType.Blocking -> {
            R.style.DialogThemeBlocker
        }
        DialogType.Warning -> {
            R.style.DialogThemeWarning
        }

        else -> {
            R.style.DialogThemeGeneral
        }
    }


    private fun getDialogBuilder(
        context: Context,
        msg: String,
        type: DialogType = DialogType.General
    ): MaterialAlertDialogBuilder {
        return MaterialAlertDialogBuilder(context, getTheme(type))
            .setTitle(context.resources.getString(R.string.app_name))
            .setCancelable(false)
            .setMessage(msg)
    }


    fun showDialog(context: Context, msg: String) {
        getDialogBuilder(context, msg)
            .setPositiveButton(context.resources.getString(R.string.ok)) { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    fun showDialog(context: Context, msg: String, type: DialogType = DialogType.General) {
        getDialogBuilder(context, msg,type)
            .setPositiveButton(context.resources.getString(R.string.ok)) { dialog, which ->
                dialog.dismiss()
            }
            .show()
    }

    fun showDialog(
        context: Context,
        msg: String,
        actionPositive: () -> Unit,
        type: DialogType = DialogType.General
    ) {
        getDialogBuilder(context, msg ,type)
            .setPositiveButton(context.resources.getString(R.string.ok)) { dialog, which ->
                actionPositive.invoke()
            }
            .show()
    }

    fun showDialog(
        context: Context,
        msg: String,
        actionPositive: () -> Unit,
        actionNegative: () -> Unit,
        type: DialogType = DialogType.General
    ) {
        getDialogBuilder(context, msg ,type)
            .setPositiveButton(context.resources.getString(R.string.ok)) { dialog, which ->
                actionPositive.invoke()
            }
            .setNegativeButton(context.resources.getString(R.string.cancel)){
                    dialog, which ->
                dialog.dismiss()
            }
            .show()
    }
}

sealed class DialogType {
    object General : DialogType()
    object Warning : DialogType()
    object Blocking : DialogType()

}