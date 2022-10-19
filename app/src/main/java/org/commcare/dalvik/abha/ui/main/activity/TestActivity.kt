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

    val action = "org.commcare.dalvik.abha.abdm.app"

    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        findViewById<Button>(R.id.intentA).setOnClickListener {
            startIntentB()
        }

        findViewById<Button>(R.id.intentB).setOnClickListener {
            startIntentA()
        }

        HeaderInterceptor.API_KEY = "Token 01bed27f81885164999b2adc0e28b8ba8cb58eda"

    }

    val lang = "hin"
    private val token = "Token 109b062f806baf3b750eaec84bebd5978de095e1"

    private fun startIntentA() {
        val intent = Intent(action).apply {
            putExtras(
                bundleOf(
                    "abha_id" to "66543673407500",
                    "lang_code" to lang,
                    "abdm_api_token" to token,
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
                    "lang_code" to lang
                )
            )
        }
        startActivityForResult(intent, REQ_CODE_B)
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