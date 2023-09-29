package org.commcare.dalvik.abha.di

import dagger.Module
import dagger.Provides
import dagger.hilt.InstallIn
import dagger.hilt.components.SingletonComponent
import okhttp3.ConnectionPool
import okhttp3.MediaType.Companion.toMediaTypeOrNull
import okhttp3.OkHttpClient
import okhttp3.Protocol
import okhttp3.Response
import okhttp3.ResponseBody
import okhttp3.logging.HttpLoggingInterceptor
import org.commcare.dalvik.abha.BuildConfig
import org.commcare.dalvik.abha.application.AbdmApplication
import org.commcare.dalvik.data.network.NetworkUtil
import org.commcare.dalvik.data.network.getMockPatientConsentResponse
import org.commcare.dalvik.data.services.HqServices
import org.commcare.dalvik.data.services.TranslationService
import retrofit2.Retrofit
import retrofit2.converter.gson.GsonConverterFactory
import java.util.concurrent.TimeUnit
import javax.inject.Named
import javax.inject.Singleton

@Module
@InstallIn(SingletonComponent::class)
object AppModule {


    @Singleton
    @Provides
    fun provideHttpLogger(): HttpLoggingInterceptor {
        return HttpLoggingInterceptor()
            .setLevel(HttpLoggingInterceptor.Level.BODY)
//            .setLevel(HttpLoggingInterceptor.Level.HEADERS)
    }

    @Singleton
    @Provides
    fun provideOkHttpClient(httpLoggingInterceptor: HttpLoggingInterceptor): OkHttpClient {
        return OkHttpClient.Builder()
            .addInterceptor(httpLoggingInterceptor)
            .connectTimeout(3, TimeUnit.MINUTES)
            .readTimeout(2, TimeUnit.MINUTES)
            .writeTimeout(2, TimeUnit.MINUTES)


            .addInterceptor { chain ->
                if (chain.request().url.host.contains("raw.githubusercontent.com")) {
                    val request = chain.request().newBuilder()
                        .addHeader("content-type", "application/json")
                        .build()
                    val response = chain.proceed(request)
                    response
                }else {
                    val request = chain.request().newBuilder()
                        .addHeader("content-type", "application/json")
                        .addHeader(
                            "Authorization",
                            "Token " + AbdmApplication.API_TOKEN
                        )
                        .build()
                    chain.proceed(request)
                }
            }
            .build()
    }

    @Singleton
    @Provides
    @Named("retrofitHq")
    fun provideRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(NetworkUtil.BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Singleton
    @Provides
    @Named("retrofitTranslation")
    fun provideTranslationRetrofit(okHttpClient: OkHttpClient): Retrofit {
        return Retrofit.Builder()
            .baseUrl(NetworkUtil.TRANSLATION_BASE_URL)
            .addConverterFactory(GsonConverterFactory.create())
            .client(okHttpClient)
            .build()
    }

    @Singleton
    @Provides
    fun provideHqServices(@Named("retrofitHq") retrofit: Retrofit): HqServices {
        return retrofit.create(HqServices::class.java)
    }

    @Singleton
    @Provides
    fun provideTranslationService(@Named("retrofitTranslation") retrofit: Retrofit): TranslationService {
        return retrofit.create(TranslationService::class.java)
    }


}
