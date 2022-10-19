package org.commcare.dalvik.data.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject


class  HeaderInterceptor @Inject constructor():Interceptor {
   companion object{
        var API_KEY= "Token 01bed27f81885164999b2adc0e28b8ba8cb58eda"
    }

    override fun intercept(chain: Interceptor.Chain): Response = chain.run {
        proceed(
            request()
                .newBuilder()
//                .addHeader("content-type", "application/json")
                .addHeader("Authorization", API_KEY)
                .build()
        )
    }


}