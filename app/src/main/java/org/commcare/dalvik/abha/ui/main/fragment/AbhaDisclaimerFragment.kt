package org.commcare.dalvik.abha.ui.main.fragment

import android.os.Bundle
import android.text.method.ScrollingMovementMethod
import android.view.View
import org.commcare.dalvik.abha.R
import org.commcare.dalvik.abha.databinding.DisclaimerBinding
import org.commcare.dalvik.abha.ui.main.activity.AbdmActivity


class AbhaDisclaimerFragment : BaseFragment<DisclaimerBinding>(DisclaimerBinding::inflate) {

    private val TAG = "DisclaimerFragment"

    override fun onClick(view: View?) {
        super.onClick(view)
        when (view?.id) {
            R.id.acceptDisclaimer -> {
                binding.disclaimerLayout.visibility = View.GONE
                binding.abhaAddressCheckLayout.visibility = View.VISIBLE
                (activity as AbdmActivity).setToolbarTitle(R.string.checkABHA)
            }
            R.id.declineDisclaimer -> {

            }
            R.id.checkABHA -> {

            }

        }

    }

    override fun onViewCreated(view: View, savedInstanceState: Bundle?) {
        super.onViewCreated(view, savedInstanceState)
        (activity as AbdmActivity).setToolbarTitle(R.string.tnc)
        binding.clickHandler = this
        binding.tnc.movementMethod = ScrollingMovementMethod()
    }
}