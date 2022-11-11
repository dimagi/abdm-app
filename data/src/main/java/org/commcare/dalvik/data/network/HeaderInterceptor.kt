package org.commcare.dalvik.data.network

import okhttp3.Interceptor
import okhttp3.Response
import javax.inject.Inject


class  HeaderInterceptor @Inject constructor():Interceptor {
   companion object{
        var API_KEY= ""
    }

    override fun intercept(chain: Interceptor.Chain): Response = chain.run {
        proceed(
            request()
                .newBuilder()
                .addHeader("Authorization", API_KEY)
                .build()
        )
    }


}