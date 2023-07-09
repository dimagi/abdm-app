package org.commcare.dalvik.abha.ui.main.activity

import android.content.res.Configuration
import android.os.Bundle
import android.view.View
import androidx.activity.OnBackPressedCallback
import androidx.activity.viewModels
import androidx.core.os.bundleOf
import androidx.lifecycle.Lifecycle
import androidx.lifecycle.lifecycleScope
import androidx.lifecycle.repeatOnLifecycle
import androidx.navigation.fragment.NavHostFragment
import androidx.navigation.ui.setupActionBarWithNavController
import dagger.hilt.android.AndroidEntryPoint
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.launch
import org.commcare.dalvik.abha.R
import org.commcare.dalvik.abha.application.AbdmApplication
import org.commcare.dalvik.abha.databinding.AbdmActivityBinding
import org.commcare.dalvik.abha.utility.DialogUtility
import org.commcare.dalvik.abha.viewmodel.AbdmViewModel
import org.commcare.dalvik.abha.viewmodel.GenerateAbhaUiState
import org.commcare.dalvik.abha.viewmodel.ScanAbhaViewModel
import org.commcare.dalvik.data.network.HeaderInterceptor
import org.commcare.dalvik.domain.model.TranslationKey
import timber.log.Timber
import java.util.Locale

@AndroidEntryPoint
class ScanAbhaActivity: BaseActivity<AbdmActivityBinding>(AbdmActivityBinding::inflate) {


    private lateinit var navHostFragment: NavHostFragment
    val viewmodel: ScanAbhaViewModel by viewModels()
    private var showMenu = true

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)

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


        intent.extras?.getString("lang_code")?.let { langId ->
            val config = Configuration(resources.configuration)
            config.locale = Locale(langId, "IN")
            resources.updateConfiguration(config, resources.displayMetrics)

//            setTitleFromIntent()

//            viewmodel.getTranslation(langId)
        }

        onBackPressedDispatcher.addCallback(this, object : OnBackPressedCallback(true) {
            override fun handleOnBackPressed() {
//                showExitSnackBar()
            }
        })

        intent.extras?.containsKey("abdm_api_token")?.let { tokenPresent ->
            if (tokenPresent) {
                intent.extras?.getString("abdm_api_token")?.let {
                    if (it.isEmpty()) {
//                        showMessageAndDispatchResult(TranslationKey.TOKEN_MISSING.toString())
                    } else {
                        AbdmApplication.API_TOKEN = it
                    }
                }
            } else {
//                showMessageAndDispatchResult(TranslationKey.TOKEN_MISSING.toString())
            }
        }

    }

    override fun getNavHostId(): Int {
        return R.id.nav_host_fragment
    }

    private fun inflateNavGraph() {
        val bundle = intent.extras ?: bundleOf()

        bundle.getString("abdm_api_token")?.let {
            HeaderInterceptor.API_KEY = it
        }



        intent.putExtras(bundle)
        val inflater = navController.navInflater

        val navGraph: Int =
            if (intent.hasExtra("abha_id")) R.navigation.scan_abha_navigation else
                R.navigation.abha_creation_navigation
        val graph = inflater.inflate(navGraph)
        graph.addInDefaultArgs(intent.extras)
        navController.setGraph(graph, bundle)
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


                    }
                }
            }
        }
    }
}