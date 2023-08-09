package org.commcare.dalvik.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import org.commcare.dalvik.domain.model.PatientConsentModel
import org.commcare.dalvik.domain.usecases.FetchPatientConsentUsecase

class ConsentPagingSource(var fetchPatientConsentUsecase: FetchPatientConsentUsecase) :
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
            val position = params.key ?: 1
            val response = fetchPatientConsentUsecase.execute("ajeet2040@sbx" )
            LoadResult.Page(
                data = response.results,
                prevKey = if (position == 1) null else -1,
                nextKey = if (position == response.count) null else position + 1
            )
        } catch (e: Exception) {
            // Handle errors in this block and return LoadResult.Error for
            LoadResult.Error(e)
        }
    }
}