package org.commcare.dalvik.domain.model

import com.google.gson.JsonObject

sealed  class HqResponseModel{
    class Success(val value:JsonObject) : HqResponseModel()
    class Error(val code:Int , val value:JsonObject) : HqResponseModel()
    class AbdmError(val code:Int , val value:AbdmErrorModel) : HqResponseModel()
    object Loading : HqResponseModel()
}

//ealed class ResultWrapper<out T> {
//    data class Success<out T>(val value: T): ResultWrapper<T>()
//    data class GenericError(val error: Exception? = null): ResultWrapper<Nothing>()
//}
