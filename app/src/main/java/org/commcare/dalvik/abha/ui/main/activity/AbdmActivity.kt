package org.commcare.dalvik.abha.ui.main.activity

import android.content.Intent
import android.content.res.Configuration
import android.os.Bundle
import android.view.Menu
import android.view.MenuItem
import android.view.View
import android.view.WindowManager
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.asFlow
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import com.google.android.material.snackbar.Snackbar
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.commcare.dalvik.abha.R
import org.commcare.dalvik.abha.application.AbdmApplication
import org.commcare.dalvik.abha.databinding.AbdmActivityBinding
import org.commcare.dalvik.abha.utility.DialogType
import org.commcare.dalvik.abha.utility.DialogUtility
import org.commcare.dalvik.abha.viewmodel.AbdmViewModel
import org.commcare.dalvik.abha.viewmodel.GenerateAbhaUiState
import org.commcare.dalvik.data.network.HeaderInterceptor
import org.commcare.dalvik.domain.model.LanguageManager
import org.commcare.dalvik.domain.model.TranslationKey
import timber.log.Timber
import java.io.Serializable
import java.util.Locale


@AndroidEntryPoint
class AbdmActivity : BaseActivity<AbdmActivityBinding>(AbdmActivityBinding::inflate) {

    private lateinit var navHostFragment: NavHostFragment
    val viewmodel: AbdmViewModel by viewModels()
    private var showMenu = true

    val ACTION_CREATE_ABHA = "create_abha"
    val ACTION_VERIFY_ABHA = "verify_abha"
    val ACTION_SCAN_ABHA = "scan_abha"
    val ACTION_GET_CONSENT = "get_consent"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

        verifyIntentData()
        mBinding?.apply {
            setSupportActionBar(this.toolbarContainer.toolbar)
        }

        navHostFragment =
            supportFragmentManager.findFragmentById(R.id.nav_host_fragment) as NavHostFragment
        navController = navHostFragment.navController
        inflateNavGraph()
        setupActionBarWithNavController(navController)
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(true)
        supportActionBar?.setHomeAsUpIndicator(R.drawable.ic_arrow_back)

//        binding.toolbarContainer.toolbar.setNavigationOnClickListener { view ->
//            Toast.makeText(this, "BACK", Toast.LENGTH_SHORT).show()
//        }

        observeLoader()
        observeBlockedOtpRequest()

        intent.extras?.getString("lang_code")?.let { langId ->
            val config = Configuration(resources.configuration)
            config.locale = Locale(langId, "IN")
            resources.updateConfiguration(config, resources.displayMetrics)

            setTitleFromIntent()

//            viewmodel.getTranslation(langId)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
                showExitSnackBar()
            }
        })

    }

    fun setTitleFromIntent() {
        intent.extras?.getString("action")?.let {
            when (it) {
                ACTION_CREATE_ABHA -> {
                    supportActionBar?.title =
                        LanguageManager.getTranslatedValue(this, R.string.ABHA_VERIFICATION)
                }

                ACTION_CREATE_ABHA -> {
                    supportActionBar?.title =
                        LanguageManager.getTranslatedValue(this, R.string.ABHA_CREATION)
                }

                ACTION_SCAN_ABHA -> {
                    supportActionBar?.title =
                        LanguageManager.getTranslatedValue(this, R.string.scanAbha)
                }
            }

        }

    }

    fun setToolbarTitle(titleId: Int) {
        supportActionBar?.title =
            LanguageManager.getTranslatedValue(this, titleId)
    }

    // MENU HANDLING
    fun hideMenu() {
        showMenu = false
        invalidateOptionsMenu()
    }

    fun hideBack() {
        supportActionBar?.setDisplayHomeAsUpEnabled(false)
        supportActionBar?.setDisplayShowHomeEnabled(false)
    }

    override fun onCreateOptionsMenu(menu: Menu?): Boolean {
        if (showMenu) {
            menuInflater.inflate(R.menu.abdm_menu, menu)
        }
        return super.onCreateOptionsMenu(menu)
    }

    override fun onOptionsItemSelected(item: MenuItem): Boolean {
        return if (item.itemId == R.id.close) {
            showExitSnackBar()
            true
        } else {
            super.onOptionsItemSelected(item)
        }
    }

    /**
     * Exit message
     */
    private fun showExitSnackBar() {
        val msg = LanguageManager.getTranslatedValue(TranslationKey.PROCEED_CLOSE)
        Snackbar.make(findViewById(android.R.id.content), msg, Snackbar.LENGTH_LONG)
            .setAction(LanguageManager.getTranslatedValue(TranslationKey.YES)) {
                showMessageAndDispatchResult(TranslationKey.USER_ABORTED)
            }.show();
    }

    /**
     * Verify intent data
     */
    private fun verifyIntentData() {
        intent.extras?.containsKey("abdm_api_token")?.let { tokenPresent ->
            if (tokenPresent) {
                intent.extras?.getString("abdm_api_token")?.let {
                    if (it.isEmpty()) {
                        showMessageAndDispatchResult(TranslationKey.TOKEN_MISSING.toString())
                    } else {
                        AbdmApplication.API_TOKEN = it
                    }
                }
            } else {
                showMessageAndDispatchResult(TranslationKey.TOKEN_MISSING.toString())
            }
        }

        intent.extras?.getString("action")?.let {
            when (it) {
                ACTION_CREATE_ABHA,
                ACTION_VERIFY_ABHA -> {
                    if (intent.extras?.containsKey("mobile_number") == false && intent.extras?.containsKey("abha_id") == false) {
                        showMessageAndDispatchResult(TranslationKey.REQ_DATA_MISSING.toString())
                    }
                 }

                ACTION_SCAN_ABHA -> {

                }
            }

        }

    }

    /**
     * Observer Blocked OTP request
     */
    private fun observeBlockedOtpRequest() {
        lifecycleScope.launch(Dispatchers.Main) {
            viewmodel.otpRequestBlocked.asFlow().collect { otpBlockedRequest ->
                otpBlockedRequest?.let {
                    DialogUtility.showDialog(
                        this@AbdmActivity,
                        resources.getString(R.string.app_blocked, it.getTimeLeftToUnblock()),
                        { dispatchResult(getErrorIntent("Blocked ,multiple OTP requested.")) },
                        DialogType.Blocking
                    )
                }

            }
        }
    }


    private fun observeLoader() {
        lifecycleScope.launch(Dispatchers.Main) {
            repeatOnLifecycle(Lifecycle.State.STARTED) {
                viewmodel.uiState.collect {
                    Timber.d("EMISSION -> ${it}")
                    when (it) {
                        is GenerateAbhaUiState.Loading -> {
                            Timber.d("LOADER VISIBILITY ${it.isLoading}")
                            binding.loader.visibility =
                                if (it.isLoading) View.VISIBLE else View.GONE
                        }

                        is GenerateAbhaUiState.Blocked -> {
                            DialogUtility.showDialog(this@AbdmActivity, "Too many OTP attempts.")
                        }

                        else -> {
                            //exhaustive block
                        }

                    }
                }
            }
        }
    }

    fun hideLoader() {
        binding.loader.visibility = View.GONE
    }


    private fun inflateNavGraph() {
        val bundle = intent.extras ?: bundleOf()

        bundle.getString("abdm_api_token")?.let {
            HeaderInterceptor.API_KEY = it
        }



        intent.putExtras(bundle)
        val inflater = navController.navInflater



        intent.extras?.getString("action")?.let {
            val navGraph = when (it) {
                ACTION_VERIFY_ABHA -> {
                    R.navigation.abha_verification_navigation
                }

                ACTION_CREATE_ABHA -> {
                    R.navigation.abha_creation_navigation
                }

                ACTION_SCAN_ABHA -> {
                    R.navigation.scan_abha_navigation
                }
                ACTION_GET_CONSENT -> {
                    R.navigation.patient_consent_navigation
                }
                else -> {
                    -1
                }
            }

            if (navGraph != -1) {
                val graph = inflater.inflate(navGraph)
                graph.addInDefaultArgs(intent.extras)
                navController.setGraph(graph, bundle)
            } else {
                Timber.d("Action not present. Navgra ")
            }

        }

    }

    fun onAbhaNumberReceived(intent: Intent) {
        dispatchResult(intent)
    }

    fun onAbhaNumberVerification(intent: Intent) {
        dispatchResult(intent)
    }

    fun onAbhaScanCompleted(intent:Intent){
        dispatchResult(intent)
    }

    override fun getNavHostId(): Int {
        return R.id.nav_host_fragment
    }

    private fun dispatchResult(intent: Intent) {
        val resultString = intent.run {
            "\nVerified =  " + getStringExtra("verified") +
                    "\nCode = " + getIntExtra("code", -1) +
                    "\nMessage = " + getStringExtra("message")
        }
        Timber.d("---- RESULT ----${resultString}")

        setResult(111, intent)
        finish()
    }

    private fun getErrorIntent(msg: String) = Intent().apply {
        putExtra("verified", "false")
        putExtra("code", AbdmResponseCode.FAILURE.value)
        putExtra("message", msg)
    }


    fun showBlockerDialog(msg: String) {
        DialogUtility.showDialog(
            this@AbdmActivity,
            msg,
            DialogType.Blocking
        )
    }


    fun showMessageAndDispatchResult(msgKey: String) {
        val msg = LanguageManager.getTranslatedValue(msgKey)
        DialogUtility.showDialog(
            this@AbdmActivity,
            msg,
            { dispatchResult(getErrorIntent(msg)) },
            DialogType.Blocking
        )
    }

    fun showMessageAndDispatchResult(msgKey: TranslationKey) {
        val msg = LanguageManager.getTranslatedValue(msgKey)
        DialogUtility.showDialog(
            this@AbdmActivity,
            msg,
            { dispatchResult(getErrorIntent(msg)) },
            DialogType.Blocking
        )
    }


}

/**
 * Mode of Verification
 */
enum class VerificationMode : Serializable {
    VERIFY_MOBILE_OTP,
    VERIFY_AADHAAR_OTP,
    CONFIRM_MOBILE_OTP,
    CONFIRM_AADHAAR_OTP
}

/**
 * Intent Response
 */
enum class AbdmResponseCode(val value: Int) {
    SUCCESS(200),
    FAILURE(333);

    companion object {
        fun fromInt(value: Int) = AbdmResponseCode.values().first { it.value == value }
    }
}