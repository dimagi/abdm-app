package org.commcare.dalvik.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
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
            val response = fetchPatientConsentUsecase.execute()
            LoadResult.Page(
                data = response.results,
                prevKey = if(response.previous == null) null else position - 1,
                nextKey = if(response.next == null) null else position + 1,
            )
        } catch (e: Exception) {
            // Handle errors in this block and return LoadResult.Error for
            LoadResult.Error(e)
        }
    }
}