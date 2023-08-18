package org.commcare.dalvik.data.network

import com.google.gson.Gson
import com.google.gson.GsonBuilder
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.flow
import org.commcare.dalvik.domain.model.AbdmErrorModel
import org.commcare.dalvik.domain.model.HqResponseModel
import retrofit2.Response
import timber.log.Timber

class NetworkUtil {
    companion object {
        const val BASE_URL = "https://ccind.duckdns.org/abdm/api/"
//                           "https://auditabdm.duckdns.org/abdm/api/"//"
        const val TRANSLATION_BASE_URL = "https://raw.githubusercontent.com/"
        fun getTranslationEndpoint(code: String) =
            "https://raw.githubusercontent.com/dimagi/abdm-app/main/resources/languages/${code}/language.json"

        //USED TO HANDLE CONSENT + ARTEFACT PAGING DATA RESPONSE ONLY
        fun <T> handleResponse(response: Response<T>):HqResponseModel {
            try {
                response.let {
                    var responseJsonObject: JsonObject
                    Timber.d("Network : ===> Response Received : code = ${response.code()}")
                    when (response.code()) {
                        200, 201  -> {
                            it.body().toString().let {
                                responseJsonObject = Gson().fromJson(it, JsonObject::class.java)
                                if (responseJsonObject.has("code")) {
                                    val adbmError: AbdmErrorModel =
                                        Gson().fromJson(it, AbdmErrorModel::class.java)
                                    return HqResponseModel.AbdmError(500, adbmError)
                                } else {
                                    return HqResponseModel.Success(responseJsonObject)
                                }
                            }

                        }

                        //USED FOR PATIENT CONSENT ERROR CASE
                        404 -> {
                            it.errorBody()?.string().let {
                                responseJsonObject = Gson().fromJson(it, JsonObject::class.java)
                                responseJsonObject.get("error")?.let {
                                    val adbmError: AbdmErrorModel =
                                        Gson().fromJson(it, AbdmErrorModel::class.java)
                                    return HqResponseModel.AbdmError(response.code(), adbmError)
                                }

                            }
                        }

                        400, 422 -> {
                            it.errorBody()?.string()?.let {
                                val gson = GsonBuilder().serializeNulls().create()
                                val adbmError: AbdmErrorModel =
                                    gson.fromJson(it, AbdmErrorModel::class.java)
                                return HqResponseModel.AbdmError(500, adbmError)
                            }

                        }

                        else -> {
                            Timber.d("Network : ==> ${response.code()} ---- ${"response.message()"}")
                            val errorJson = JsonObject().apply {
                                var msg = response.message()
                                if (msg.isNullOrEmpty()) {
                                    msg = "Error : ${response.code()}   ${response.message()}"
                                }
                                addProperty("message", msg)
                            }
                            return HqResponseModel.Error(555, errorJson)
                        }
                    }
                }
            } catch (t: Throwable) {
                Timber.d("Network : ==> Exception ---- ${t.message}")
                val errJson = JsonObject()
                errJson.addProperty("message", t.message)
                return HqResponseModel.Error(555, errJson)
            }

            val genError = JsonObject()
            genError.addProperty("message", "Response issue.")
            return HqResponseModel.Error(555,genError )
        }

    }
}


fun <T> safeApiCall(call: suspend () -> Response<T>) = flow {
    this.emit(HqResponseModel.Loading)
    try {
        val response = call.invoke()
        response.let {
            var responseJsonObject: JsonObject
            Timber.d("Network : ===> Response Received : code = ${response.code()}")
            when (response.code()) {
                200, 201 -> {
                    it.body().toString().let {
                        responseJsonObject = Gson().fromJson(it, JsonObject::class.java)
                        if (responseJsonObject.has("code")) {
                            val adbmError: AbdmErrorModel =
                                Gson().fromJson(it, AbdmErrorModel::class.java)
                            emit(HqResponseModel.AbdmError(500, adbmError))
                        } else {
                            emit(HqResponseModel.Success(responseJsonObject))
                        }
                    }

                }

                400, 422 -> {
                    it.errorBody()?.string()?.let {
                        val gson = GsonBuilder().serializeNulls().create()
                        val adbmError: AbdmErrorModel =
                            gson.fromJson(it, AbdmErrorModel::class.java)
                        emit(HqResponseModel.AbdmError(500, adbmError))
                    }

                }

                else -> {
                    Timber.d("Network : ==> ${response.code()} ---- ${"response.message()"}")
                    val errorJson = JsonObject().apply {
                        var msg = response.message()
                        if (msg.isNullOrEmpty()) {
                            msg = "Error : ${response.code()}   ${response.message()}"
                        }
                        addProperty("message", msg)
                    }
                    emit(HqResponseModel.Error(555, errorJson))
                }
            }

        }

    } catch (t: Throwable) {
        Timber.d("Network : ==> Exception ---- ${t.message}")
        val errJson = JsonObject()
        errJson.addProperty("message", t.message)
        emit(HqResponseModel.Error(555, errJson))
    }
}

// MOCK RESPONSES
const val getMockPatientConsentResponse = """
    {
    "count": 7,
    "next": "http://localhost:8000/abdm/api/hiu/consents?abha_id=ajeet2040%40sbx&from_date=2022-07-10&page=2&search=consult&to_date=2023-05-18",
    "previous": null,
    "results": [
        {
            "id": 9,
            "consent_request_id": "65641985-ac23-4936-a5ff-688b6f69f7d9",
            "date_created": "2023-07-31T15:20:06.321526Z",
            "last_modified": "2023-07-31T15:21:42.243798Z",
            "status": "GRANTED",
            "details": {
                "hiu": {
                    "id": "Ashish-HIU-Registered"
                },
                "hiTypes": [
                    "OPConsultation"
                ],
                "patient": {
                    "id": "ajeet2040@sbx"
                },
                "purpose": {
                    "code": "CAREMGT",
                    "text": "Care Management",
                    "refUri": "http://terminology.hl7.org/ValueSet/v3-PurposeOfUse"
                },
                "requester": {
                    "name": "Dr. Manju",
                    "identifier": {
                        "type": "REGNO",
                        "value": "MH1001",
                        "system": "https://www.mciindia.org"
                    }
                },
                "permission": {
                    "dateRange": {
                        "to": "1691334534837",
                        "from": "1691334534837"
                    },
                    "frequency": {
                        "unit": "HOUR",
                        "value": 1,
                        "repeats": 0
                    },
                    "accessMode": "VIEW",
                    "dataEraseAt": "1691334534837"
                }
            },
            "error": null,
            "health_info_from_date": "2023-05-17T15:12:43.960000Z",
            "health_info_to_date": "2023-07-17T15:12:43.961000Z",
            "health_info_types": [
                "OPConsultation"
            ],
            "expiry_date": "1691334534837",
            "user": "ajeet"
        },
        {
            "id": 8,
            "consent_request_id": null,
            "date_created": "2023-07-31T15:08:29.650401Z",
            "last_modified": "2023-07-31T15:08:29.650487Z",
            "status": "PENDING",
            "details": {
                "hiu": {
                    "id": "Ashish-HIU-Registered"
                },
                "hiTypes": [
                    "OPConsultation"
                ],
                "patient": {
                    "id": "ajeet2040@sbx"
                },
                "purpose": {
                    "code": "CAREMGT",
                    "text": "Care Management",
                    "refUri": "http://terminology.hl7.org/ValueSet/v3-PurposeOfUse"
                },
                "requester": {
                    "name": "Dr. Manju",
                    "identifier": {
                        "type": "REGNO",
                        "value": "MH1001",
                        "system": "https://www.mciindia.org"
                    }
                },
                "permission": {
                    "dateRange": {
                        "to": "1691334534837",
                        "from": "1691334534837"
                    },
                    "frequency": {
                        "unit": "HOUR",
                        "value": 1,
                        "repeats": 0
                    },
                    "accessMode": "VIEW",
                    "dataEraseAt": "1691334534837"
                }
            },
            "error": null,
            "health_info_from_date": "2023-05-17T15:12:43.960000Z",
            "health_info_to_date": "2023-07-17T15:12:43.961000Z",
            "health_info_types": [
                "OPConsultation"
            ],
            "expiry_date": "1691334534837",
            "user": "ajeet"
        }
    ]
}
    """

const val getMockSubmitConsentResponse = """
{
    "id": 19,
    "gateway_request_id": "6dfd070b-a6db-48bf-a10f-f83ac38bdb7d",
    "consent_request_id": null,
    "date_created": "2023-07-27T06:48:39.923358Z",
    "last_modified": "2023-07-27T06:48:39.923405Z",
    "status": "PENDING",
    "details": {
        "purpose": {
            "code": "CAREMGT",
            "refUri": "http://terminology.hl7.org/ValueSet/v3-PurposeOfUse",
            "text": "Care Management"
        },
        "patient": {
            "id": "ajeet2040@sbx"
        },
        "hiu": {
            "id": "Ashish-HIU-Registered"
        },
        "requester": {
            "name": "Dr. Manju",
            "identifier": {
                "type": "REGNO",
                "value": "MH1001",
                "system": "https://www.mciindia.org"
            }
        },
        "hiTypes": [
            "OPConsultation"
        ],
        "permission": {
            "accessMode": "VIEW",
            "dateRange": {
                "from": "2022-05-17T15:12:43.960000Z",
                "to": "2023-07-17T15:12:43.961000Z"
            },
            "dataEraseAt": "2023-08-18T18:12:43.961000Z",
            "frequency": {
                "unit": "HOUR",
                "value": 1,
                "repeats": 0
            }
        }
    },
    "error": null,
    "user": "ajeet"
}
"""


