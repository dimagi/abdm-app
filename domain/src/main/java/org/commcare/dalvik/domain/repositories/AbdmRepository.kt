package org.commcare.dalvik.domain.repositories

import androidx.lifecycle.LiveData
import androidx.paging.PagingData
import com.google.gson.JsonObject
import kotlinx.coroutines.flow.Flow
import org.commcare.dalvik.domain.model.*
import org.commcare.dalvik.domain.usecases.FetchConsentArtefactsUsecase
import org.commcare.dalvik.domain.usecases.FetchPatientConsentUsecase
import retrofit2.http.Query

interface AbdmRepository {
    fun generateMobileOtp(mobileModel: MobileOtpRequestModel): Flow<HqResponseModel>
    fun generateAadhaarOtp(aadhaarModel: AadhaarOtpRequestModel): Flow<HqResponseModel>
    fun verifyMobileOtp(verifyOtpRequestModel: VerifyOtpRequestModel): Flow<HqResponseModel>
    fun verifyAadhaarOtp(verifyOtpRequestModel: VerifyOtpRequestModel): Flow<HqResponseModel>
    fun getAuthenticationMethods(authMethodRequestModel: GetAuthMethodRequestModel): Flow<HqResponseModel>
    fun generateAuthOtp(generateAuthOtp: GenerateAuthOtpModel): Flow<HqResponseModel>
    fun confirmMobileOtp(otpModel: VerifyOtpRequestModel): Flow<HqResponseModel>
    fun confirmAadhaarOtp(otpModel: VerifyOtpRequestModel): Flow<HqResponseModel>
    fun checkAbhaAvailability(abhaVerificationRequestModel: AbhaVerificationRequestModel): Flow<HqResponseModel>
    fun fetchAbhaCard(abhaCardRequestModel: AbhaCardRequestModel): Flow<HqResponseModel>
    fun submitPatientConsent(patientConsentDetailModel: PatientConsentDetailModel): Flow<HqResponseModel>
    suspend fun getPatientConsents(
        @Query("abha_id") abhaId: String,
        @Query("page") page: Int?,
        @Query("search") searchText: String?,
        @Query("from_date") fromDate: String?,
        @Query("to_date") toDate: String?
    ): HqResponseModel

    fun getPatientConsentPagerData(
        fetchPatientConsentUsecase: FetchPatientConsentUsecase
    ): LiveData<PagingData<PatientConsentModel>>


    suspend fun getConsentArtefacts(
        @Query("consent_request_id") consentRequestId: String,
        @Query("search") searchText: String?,
        @Query("page")page:Int?
    ): ConsentArtefactsList

    fun getConsentArtefactPagerData(
        fetchPatientConsentUsecase: FetchConsentArtefactsUsecase
    ): LiveData<PagingData<ConsentArtefactModel>>

    fun getPatientHealthData(artefactId:String,transactionId:String?,page:Int?):Flow<HqResponseModel>

    fun getCCAuthModes(ccAuthModesRequestModel: CCAuthModesRequestModel):Flow<HqResponseModel>

    fun generateCCAuthenticationOtp(ccAuthModesRequestModel: CCAuthModesRequestModel):Flow<HqResponseModel>

    fun confirmCCAuthenticationOtp(confirmAuthModel: ConfirmAuthModel):Flow<HqResponseModel>

    fun linkCareContext(ccLinkModel:CCLinkModel):Flow<HqResponseModel>

}