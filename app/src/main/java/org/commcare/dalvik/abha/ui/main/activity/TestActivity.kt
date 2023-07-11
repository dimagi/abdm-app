package org.commcare.dalvik.abha.ui.main.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import org.commcare.dalvik.abha.R
import org.commcare.dalvik.data.network.HeaderInterceptor
import timber.log.Timber

class TestActivity : AppCompatActivity() {

    val REQ_CODE_A = 100
    val REQ_CODE_B = 101
    val REQ_CODE_C = 102

    val action = "org.commcare.dalvik.abha.abdm.app"


    val ACTION_CREATE_ABHA = "create_abha"
    val ACTION_VERIFY_ABHA = "verify_abha"
    val ACTION_SCAN_ABHA = "scan_abha"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        findViewById<Button>(R.id.intentA).setOnClickListener {
            startIntentB()
        }

        findViewById<Button>(R.id.intentB).setOnClickListener {
            startIntentA()
        }

        findViewById<Button>(R.id.intentC).setOnClickListener {
            startIntentC()
        }

        HeaderInterceptor.API_KEY = ""

    }

    val lang = "hi"
    private val token = "0c0a3fbbacc0922192a1b4e63be5d6f511790a31"

    private fun startIntentA() {
        val intent = Intent(action).apply {
            putExtras(
                bundleOf(
                    "abha_id" to "",
                    "lang_code" to lang,
                    "abdm_api_token" to token,
                    "action" to ACTION_CREATE_ABHA
                )
            )
        }
        startActivityForResult(intent, REQ_CODE_A)

    }

    private fun startIntentB() {

        val intent = Intent(action).apply {
            putExtras(
                bundleOf(
                    "mobile_number" to "9560833229",
                    "abdm_api_token" to token,
                    "lang_code" to lang,
                    "action" to ACTION_VERIFY_ABHA
                )
            )
        }
        startActivityForResult(intent, REQ_CODE_B)
    }

    private fun startIntentC() {

        val intent = Intent(action).apply {
            putExtras(
                bundleOf(
                    "abdm_api_token" to token,
                    "lang_code" to lang,
                    "action" to ACTION_SCAN_ABHA
                )
            )
        }
        startActivityForResult(intent, REQ_CODE_C)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        Timber.d("OnActivityResult--------${resultCode}")

        when (requestCode) {
            REQ_CODE_A -> {

            }

            REQ_CODE_B -> {

            }
        }

    }

}