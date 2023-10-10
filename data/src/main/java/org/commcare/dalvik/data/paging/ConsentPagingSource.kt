package org.commcare.dalvik.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import com.google.gson.Gson
import org.commcare.dalvik.domain.model.HqResponseModel
import org.commcare.dalvik.domain.model.PatientConsentList
import org.commcare.dalvik.domain.model.PatientConsentModel
import org.commcare.dalvik.domain.usecases.FetchPatientConsentUsecase
import timber.log.Timber


private const val DEFAULT_FIRST_PAGE = 1;

class ConsentPagingSource(
    var fetchPatientConsentUsecase: FetchPatientConsentUsecase
) :
    PagingSource<Int, PatientConsentModel>() {


    override fun getRefreshKey(state: PagingState<Int, PatientConsentModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, PatientConsentModel> {
        return try {
            // Start refresh at page 1 if undefined.
            Timber.d("PARAMs KEY = ${params.key}")
            val position = params.key ?: DEFAULT_FIRST_PAGE
            val page = if(position == DEFAULT_FIRST_PAGE) null else position
            val response = fetchPatientConsentUsecase.execute(page)

            return when (response) {
                is HqResponseModel.Success -> {
                    val patientConsentList = Gson().fromJson(response.value, PatientConsentList::class.java)

                    LoadResult.Page(
                        data = patientConsentList.results,
                        prevKey = if (patientConsentList.previous == null) null else position - 1,
                        nextKey = if (patientConsentList.next == null) null else position + 1,
                    )

                }

                is HqResponseModel.Error -> {
                    LoadResult.Error(RuntimeException(response.value.get("message").toString()))
                }

                is HqResponseModel.AbdmError -> {
                    LoadResult.Error(RuntimeException(response.value.message))
                }

                else -> LoadResult.Error(RuntimeException("Error while loading..."))
            }
            //TODO : Remove once Paging source is stable. Else revert to this impl
//            LoadResult.Page(
//                data = response.results,
//                prevKey = if(response.previous == null) null else position - 1,
//                nextKey = if(response.next == null) null else position + 31,
//            )
        } catch (e: Exception) {
            // Handle errors in this block and return LoadResult.Error for
            LoadResult.Error(e)
        }
    }
}