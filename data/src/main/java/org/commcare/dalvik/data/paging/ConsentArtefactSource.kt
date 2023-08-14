package org.commcare.dalvik.data.paging

import androidx.paging.PagingSource
import androidx.paging.PagingState
import org.commcare.dalvik.domain.model.ConsentArtefactModel
import org.commcare.dalvik.domain.usecases.FetchConsentArtefactsUsecase
import timber.log.Timber

private const val DEFAULT_FIRST_PAGE = 1;

class ConsentArtefactSource(var fetchConsentArtefactsUsecase: FetchConsentArtefactsUsecase) :
    PagingSource<Int, ConsentArtefactModel>() {


    override fun getRefreshKey(state: PagingState<Int, ConsentArtefactModel>): Int? {
        return state.anchorPosition?.let { anchorPosition ->
            val anchorPage = state.closestPageToPosition(anchorPosition)
            anchorPage?.prevKey?.plus(1) ?: anchorPage?.nextKey?.minus(1)
        }
    }

    override suspend fun load(params: LoadParams<Int>): LoadResult<Int, ConsentArtefactModel> {
        return try {
            // Start refresh at page 1 if undefined.
            Timber.d("PARAMs KEY = ${params.key}")
            val position = params.key ?: DEFAULT_FIRST_PAGE
            val response = fetchConsentArtefactsUsecase.execute()
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