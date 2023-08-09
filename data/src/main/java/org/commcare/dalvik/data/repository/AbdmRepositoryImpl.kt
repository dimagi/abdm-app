package org.commcare.dalvik.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.PagingSource
import androidx.paging.liveData
import com.google.gson.Gson
import com.google.gson.JsonObject
import kotlinx.coroutines.GlobalScope
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.launch
import org.commcare.dalvik.data.network.safeApiCall
import org.commcare.dalvik.data.paging.ConsentPagingSource
import org.commcare.dalvik.data.services.HqServices
import org.commcare.dalvik.domain.model.*
import org.commcare.dalvik.domain.repositories.AbdmRepository
import org.commcare.dalvik.domain.usecases.FetchPatientConsentUsecase
import timber.log.Timber
import javax.inject.Inject

class AbdmRepositoryImpl @Inject constructor(val hqServices: HqServices) : AbdmRepository {


    override fun generateMobileOtp(mobileModel: MobileOtpRequestModel): Flow<HqResponseModel> =
        safeApiCall {
            hqServices.generateMobileOtp(mobileModel)
        }


    override fun generateAadhaarOtp(aadhaarModel: AadhaarOtpRequestModel): Flow<HqResponseModel> =
        safeApiCall {
            hqServices.generateAadhaarOtp(aadhaarModel)
        }

    override fun getAuthenticationMethods(authMethodRequestModel: GetAuthMethodRequestModel): Flow<HqResponseModel> =
        safeApiCall {
            hqServices.getAuthenticationMethods(authMethodRequestModel)
        }

    override fun generateAuthOtp(generateAuthOtp: GenerateAuthOtpModel): Flow<HqResponseModel> =
        safeApiCall {
            hqServices.generateAuthOtp(generateAuthOtp)
        }

    override fun confirmMobileOtp(otpModel: VerifyOtpRequestModel): Flow<HqResponseModel> =
        safeApiCall {
            hqServices.confirmMobileOtp(otpModel)
        }


    override fun confirmAadhaarOtp(otpModel: VerifyOtpRequestModel): Flow<HqResponseModel> =
        safeApiCall {
            hqServices.confirmAadhaarOtp(otpModel)
        }


    override fun verifyMobileOtp(verifyOtpRequestModel: VerifyOtpRequestModel): Flow<HqResponseModel> =
        safeApiCall {
            hqServices.verifyMobileOtp(verifyOtpRequestModel)
        }

    override fun verifyAadhaarOtp(verifyOtpRequestModel: VerifyOtpRequestModel): Flow<HqResponseModel> =
        safeApiCall {
            hqServices.verifyAadhaarOtp(verifyOtpRequestModel)
        }

    override fun checkAbhaAvailability(abhaVerificationRequestModel: AbhaVerificationRequestModel): Flow<HqResponseModel> =
        safeApiCall {
            hqServices.checkAbhaAddressAvailability(abhaVerificationRequestModel)
        }

    override fun fetchAbhaCard(abhaCardRequestModel: AbhaCardRequestModel): Flow<HqResponseModel> =
        safeApiCall {
            hqServices.fetchAbhaCard(abhaCardRequestModel)
        }

    override fun submitPatientConsent(patientConsentDetailModel: PatientConsentDetailModel): Flow<HqResponseModel> =
        safeApiCall {
            hqServices.generatePatientConsent(patientConsentDetailModel)
        }

    override suspend fun getPatientConsents(
        abhaId: String,
        searchText: String?,
        fromDate: String?,
        toDate: String?
    ): PatientConsentList {
        val result = hqServices.getPatientConsents(abhaId, searchText, fromDate, toDate)
        Timber.d("RESULT : ${result.body().toString()}")
        val patientConsentList = Gson().fromJson(result.body(), PatientConsentList::class.java)

        return patientConsentList

    }


    override fun getPatientConsent(
        fetchPatientConsentUsecase: FetchPatientConsentUsecase
    ) = Pager(
        config = PagingConfig(pageSize = 10, maxSize = 100),
        pagingSourceFactory = { ConsentPagingSource(fetchPatientConsentUsecase) }
    ).liveData


}