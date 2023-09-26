package org.commcare.dalvik.data.services

import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow
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

    @POST("exists_by_health_id")
    suspend fun checkAbhaAddressAvailability(@Body abhaVerificationRequestModel: AbhaVerificationRequestModel):Response<JsonObject>

    @POST("get_health_card_png")
    suspend fun fetchAbhaCard(@Body abhaCardRequestModel: AbhaCardRequestModel):Response<JsonObject>

    @POST("hiu/generate_consent_request")
    suspend fun generatePatientConsent(@Body patientConsentDetailModel: PatientConsentDetailModel):Response<JsonObject>

    @GET("hiu/consents")
    suspend fun getPatientConsents(
        @Query("abha_id") abhaId: String,
        @Query("page") page: Int?,
        @Query("search") searchText: String? ,
        @Query("from_date") fromDate: String? ,
        @Query("to_date") toDate: String?
    ): Response<JsonObject>


    @GET("hiu/consent_artefacts")
    suspend fun getConsentArtefacts(
        @Query("consent_request_id") consentRequestId: String,
        @Query("search") searchText: String?,
        @Query("page") page: Int?,
    ):Response<JsonObject>

    @GET("hiu/health-information/request")
    suspend fun getHealthData(
        @Query("artefact_id") artefactId: String,
        @Query("transaction_id") transactionId: String?,
        @Query("page") page: Int?
    ):Response<JsonObject>



    @POST("user_auth/fetch_auth_modes")
    suspend fun getCareContextAuthModes(@Body ccAuthModesRequestModel: CCAuthModesRequestModel):Response<JsonObject>

    @POST("user_auth/auth_init")
    suspend fun initAuth(@Body ccAuthModesRequestModel: CCAuthModesRequestModel):Response<JsonObject>
    @POST("user_auth/confirm_auth")
    suspend fun confirmAuth(@Body ccAuthModesRequestModel: CCAuthModesRequestModel):Response<JsonObject>

    @POST("hip/link_care_context")
    suspend fun  linkCareContext():Response<JsonObject>



}