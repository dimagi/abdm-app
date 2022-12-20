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
        const val TRANSLATION_BASE_URL = "https://raw.githubusercontent.com/"
        fun getTranslationEndpoint(code:String)=
            "https://raw.githubusercontent.com/dimagi/abdm-app/main/resources/languages/${code}/language.json"
    }
}

fun <T> safeApiCall(call: suspend () -> Response<T>) = flow {
    this.emit(HqResponseModel.Loading)
    try {
        val response = call.invoke()
        response.let {
            var responseJsonObject:JsonObject
            Timber.d("Network : ===> Response Received : code = ${response.code()}")
            when(response.code()){
                200 ->{
                    it.body().toString().let {
                        responseJsonObject = Gson().fromJson(it, JsonObject::class.java)
                        if(responseJsonObject.has("code")){
                            val adbmError:AbdmErrorModel =  Gson().fromJson(it, AbdmErrorModel::class.java)
                            emit(HqResponseModel.AbdmError(500 , adbmError))
                        }else {
                            emit(HqResponseModel.Success(responseJsonObject))
                        }
                    }

                }
                400,422->{
                    it.errorBody()?.string()?.let{
                        val gson = GsonBuilder().serializeNulls().create()
                        val adbmError:AbdmErrorModel = gson.fromJson(it, AbdmErrorModel::class.java)
                        emit(HqResponseModel.AbdmError(500 , adbmError))
                    }

                }
                else ->{
                    Timber.d("Network : ==> ${response.code()} ---- ${"response.message()"}")
                    val errorJson = JsonObject().apply {
                        var msg = response.message()
                        if(msg.isNullOrEmpty()){
                            msg = "Error : ${response.code()}"
                        }
                        addProperty("message",msg)
                    }
                    emit(HqResponseModel.Error(555,errorJson))
                }
            }

        }

    } catch (t: Throwable) {
        Timber.d("Network : ==> Exception ---- ${t.message}")
        val errJson = JsonObject()
        errJson.addProperty("message",t.message)
        emit(HqResponseModel.Error(555,errJson))
    }
}


