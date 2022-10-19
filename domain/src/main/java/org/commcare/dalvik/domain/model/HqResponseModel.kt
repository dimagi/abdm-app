package org.commcare.dalvik.domain.model

import com.google.gson.JsonObject

sealed  class HqResponseModel{
    class Success(val value:JsonObject) : HqResponseModel()
    class Error(val code:Int , val value:JsonObject) : HqResponseModel()
    class AbdmError(val code:Int , val value:AbdmErrorModel) : HqResponseModel()
    object Loading : HqResponseModel()
}

