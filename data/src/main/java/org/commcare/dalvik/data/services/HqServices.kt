package org.commcare.dalvik.data.services

import com.google.gson.JsonObject
import org.commcare.dalvik.domain.model.*
import retrofit2.Response
import retrofit2.http.*

interface HqServices {

    @POST("generate_mobile_otp")
    suspend fun generateMobileOtp(@Body mobileModel: MobileOtpRequestModel):Response<JsonObject>

    @POST("generate_aadhaar_otp")
    suspend fun generateAadhaarOtp(@Body aadhaarModel: AadhaarOtpRequestModel):Response<JsonObject>

    @POST("verify_aadhaar_otp")
    suspend fun verifyAadhaarOtp(@Body verifyOtpRequestModel: VerifyOtpRequestModel):Response<JsonObject>

    @POST("verify_mobile_otp")
    suspend fun verifyMobileOtp(@Body verifyOtpRequestModel: VerifyOtpRequestModel):Response<JsonObject>

    @POST("generate_auth_otp")
    suspend fun generateAuthOtp(@Body generateAuthOtpModel: GenerateAuthOtpModel):Response<JsonObject>

    @POST("search_health_id")
    suspend fun getAuthenticationMethods(@Body authMethodRequestModel: GetAuthMethodRequestModel):Response<JsonObject>

    @POST("confirm_with_aadhaar_otp")
    suspend fun confirmAadhaarOtp(@Body verifyOtpRequestModel: VerifyOtpRequestModel):Response<JsonObject>

    @POST("confirm_with_mobile_otp")
    suspend fun confirmMobileOtp(@Body verifyOtpRequestModel: VerifyOtpRequestModel):Response<JsonObject>

}