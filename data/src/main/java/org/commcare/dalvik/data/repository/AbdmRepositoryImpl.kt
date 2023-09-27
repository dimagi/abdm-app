package org.commcare.dalvik.data.repository

import androidx.paging.Pager
import androidx.paging.PagingConfig
import androidx.paging.liveData
import com.google.gson.Gson
import kotlinx.coroutines.flow.Flow
import org.commcare.dalvik.data.network.NetworkUtil
import org.commcare.dalvik.data.network.safeApiCall
import org.commcare.dalvik.data.paging.ConsentArtefactSource
import org.commcare.dalvik.data.paging.ConsentPagingSource
import org.commcare.dalvik.data.services.HqServices
import org.commcare.dalvik.domain.model.AadhaarOtpRequestModel
import org.commcare.dalvik.domain.model.AbhaCardRequestModel
import org.commcare.dalvik.domain.model.AbhaVerificationRequestModel
import org.commcare.dalvik.domain.model.CCAuthModesRequestModel
import org.commcare.dalvik.domain.model.CCLinkModel
import org.commcare.dalvik.domain.model.ConfirmAuthModel
import org.commcare.dalvik.domain.model.ConsentArtefactsList
import org.commcare.dalvik.domain.model.GenerateAuthOtpModel
import org.commcare.dalvik.domain.model.GetAuthMethodRequestModel
import org.commcare.dalvik.domain.model.HqResponseModel
import org.commcare.dalvik.domain.model.MobileOtpRequestModel
import org.commcare.dalvik.domain.model.PatientConsentDetailModel
import org.commcare.dalvik.domain.model.PatientConsentList
import org.commcare.dalvik.domain.model.VerifyOtpRequestModel
import org.commcare.dalvik.domain.repositories.AbdmRepository
import org.commcare.dalvik.domain.usecases.FetchConsentArtefactsUsecase
import org.commcare.dalvik.domain.usecases.FetchPatientConsentUsecase
import retrofit2.http.Query
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

    override  fun getPatientHealthData(artefactId:String,transactionId:String?,page:Int?):Flow<HqResponseModel> =
        safeApiCall {
            hqServices.getHealthData(artefactId,transactionId,page)
        }

    override fun getCCAuthModes(ccAuthModesRequestModel: CCAuthModesRequestModel): Flow<HqResponseModel> = safeApiCall {
        hqServices.getCareContextAuthModes(ccAuthModesRequestModel)
    }
    override fun generateCCAuthenticationOtp(ccAuthModesRequestModel: CCAuthModesRequestModel)= safeApiCall {
        hqServices.initCareContextAuth(ccAuthModesRequestModel)
    }

    override fun confirmCCAuthenticationOtp(confirmAuthModel: ConfirmAuthModel)= safeApiCall {
        hqServices.confirmCareContextAuth(confirmAuthModel)
    }

    override fun linkCareContext(ccLinkModel: CCLinkModel)= safeApiCall {
        hqServices.linkCareContext(ccLinkModel)
    }

    override suspend fun getPatientConsents(
        abhaId: String,
        page:Int?,
        searchText: String?,
        fromDate: String?,
        toDate: String?
    ): HqResponseModel {
        val result = hqServices.getPatientConsents(abhaId, page,searchText, fromDate, toDate)
        return NetworkUtil.handleResponse(result)
        //TODO : Remove once Paging source is stable. Else revert to this impl
//        Timber.d("RESULT : ${response}")
//        return Gson().fromJson(result.body(), PatientConsentList::class.java)
    }

    override fun getPatientConsentPagerData(
        fetchPatientConsentUsecase: FetchPatientConsentUsecase
    ) = Pager(
        config = PagingConfig(pageSize = 10, maxSize = 100),
        pagingSourceFactory = { ConsentPagingSource(fetchPatientConsentUsecase) }
    ).liveData


    override suspend fun getConsentArtefacts(
        consentRequestId: String,
        searchText: String?,
        page:Int?
    ): ConsentArtefactsList {
        val result = hqServices.getConsentArtefacts(consentRequestId, searchText,page)
        Timber.d("RESULT : ${result.body().toString()}")
        return Gson().fromJson(result.body(), ConsentArtefactsList::class.java)
    }


    override fun getConsentArtefactPagerData(fetchPatientConsentUsecase: FetchConsentArtefactsUsecase) =
        Pager(
            config = PagingConfig(pageSize = 10, maxSize = 100),
            pagingSourceFactory = { ConsentArtefactSource(fetchPatientConsentUsecase) }
        ).liveData


}