package org.commcare.dalvik.abha.ui.main.activity

import android.content.Intent
import android.os.Bundle
import android.widget.Button
import androidx.appcompat.app.AppCompatActivity
import androidx.core.os.bundleOf
import org.commcare.dalvik.abha.R
import org.commcare.dalvik.data.network.HeaderInterceptor
import org.json.JSONArray
import org.json.JSONObject
import timber.log.Timber

class TestActivity : AppCompatActivity() {

    val REQ_CODE_A = 100
    val REQ_CODE_B = 101
    val REQ_CODE_C = 102
    val REQ_CODE_D = 103
    val REQ_CODE_E = 104
    val REQ_NOTIFY_PATIENT = 105

    val action = "org.commcare.dalvik.abha.abdm.app"


    val ACTION_CREATE_ABHA = "create_abha"
    val ACTION_VERIFY_ABHA = "verify_abha"
    val ACTION_SCAN_ABHA = "scan_abha"
    val ACTION_GET_CONSENT = "get_consent"
    val ACTION_NOTIFY_PATIENT = "notify_patient"
    val ACTION_CARE_CONTEXT_LINK = "link_care_context"


    override fun onCreate(savedInstanceState: Bundle?) {
        super.onCreate(savedInstanceState)
        setContentView(R.layout.activity_test)

        findViewById<Button>(R.id.intentA).setOnClickListener {
            verifyABHAIntent()
        }

        findViewById<Button>(R.id.intentB).setOnClickListener {
            createABHAIntent()
        }

        findViewById<Button>(R.id.intentC).setOnClickListener {
            startIntentC()
        }

        findViewById<Button>(R.id.intentD).setOnClickListener {
            createConsentIntent()
        }

        findViewById<Button>(R.id.intentE).setOnClickListener {
            linkCareContext()
        }

        findViewById<Button>(R.id.notifyPatient).setOnClickListener {
            notifyPatient()
        }

        HeaderInterceptor.API_KEY = ""

    }

    private fun notifyPatient() {
        val intent = Intent(action).apply {
            putExtras(
                bundleOf(
                    "phoneNo" to "9560833229",
                    "hip" to "6004",
                    "lang_code" to lang,
                    "abdm_api_token" to token,
                    "action" to ACTION_NOTIFY_PATIENT
                )
            )
        }
        startActivityForResult(intent, REQ_NOTIFY_PATIENT)
    }

    val lang = "en"
    private val token = "33ec318e4ea1261d1e200c4eff017ea696fedaa9" //"cba903c996da17ca535d4bbb1b04d8e0eb7127ce"



    private fun createABHAIntent() {
        val intent = Intent(action).apply {
            putExtras(
                bundleOf(
                    "mobile_number" to "9560833229",
                    "abha_id" to "harish23@sbx",
                    "lang_code" to lang,
                    "abdm_api_token" to token,
                    "action" to ACTION_CREATE_ABHA
                )
            )
        }
        startActivityForResult(intent, REQ_CODE_A)

    }

    private fun verifyABHAIntent() {

        val intent = Intent(action).apply {
            putExtras(
                bundleOf(
                    "abha_id" to "harish23@sbx",
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

    private fun createConsentIntent(){

        val intent = Intent(action).apply {
            putExtras(
                bundleOf(
                    "patient_name" to "Test Patient",
                    "abha_id" to "ajeet2042@sbx",
                    "hiu_id" to "Ashish-HIU-Registered",
                    "abdm_api_token" to token,
                    "lang_code" to lang,
                    "action" to ACTION_GET_CONSENT,
                    "requester" to "Dr xyz"
                )
            )
        }
        startActivityForResult(intent, REQ_CODE_C)

    }

    private fun linkCareContext(){
        val intent = Intent(action).apply {
            val patientJson  = JSONObject().apply {
                put("referenceNumber","Test_001")
                put("display","Ajeet Test_001")


                val careContext= JSONObject().apply {
                    put("referenceNumber","CC_507")
                    put("display","Visit for fever 505")
                    val hiTypes= JSONArray().apply {
                        put("Prescription")
                    }
                    put("hiTypes",hiTypes)

                    val additionalInfo =  JSONObject().apply {
                        put("domain","domain  data")
                        put("record_date","2023-10-31T21:37:41.786Z")
                        // 2023-10-31T21:37:41.786Z
                    }

                    put("additionalInfo",additionalInfo)

                }


                val careContextArr= JSONArray().apply {
                    put(careContext)
                }
                put("careContexts",careContextArr)

            }
            putExtras(
                bundleOf(
                    "abhaId" to "91766261606756@sbx",
                    "purpose" to "LINK",
                    "hipId" to "6004",
                    "patientDetail" to patientJson.toString(),
                    "lang_code" to lang,
                    "action" to ACTION_CARE_CONTEXT_LINK,
                    "abdm_api_token" to token,
                )
            )
        }
        startActivityForResult(intent, REQ_CODE_E)
    }

    override fun onActivityResult(requestCode: Int, resultCode: Int, intent: Intent?) {
        super.onActivityResult(requestCode, resultCode, intent)
        Timber.d("OnActivityResult--------${resultCode}")

        when (requestCode) {
            REQ_CODE_A -> {

            }

            REQ_CODE_B -> {

            }

            REQ_NOTIFY_PATIENT -> {

            }
        }

    }

}