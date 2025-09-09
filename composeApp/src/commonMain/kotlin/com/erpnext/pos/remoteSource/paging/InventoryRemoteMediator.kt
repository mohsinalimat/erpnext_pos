package com.erpnext.pos.remoteSource.paging

import androidx.paging.ExperimentalPagingApi
import androidx.paging.LoadType
import androidx.paging.PagingState
import androidx.paging.RemoteMediator
import com.erpnext.pos.localSource.dao.ItemDao
import com.erpnext.pos.localSource.entities.ItemEntity
import com.erpnext.pos.remoteSource.api.APIService
import com.erpnext.pos.remoteSource.dto.ItemDto
import com.erpnext.pos.remoteSource.mapper.toEntity
import kotlinx.io.IOException

@OptIn(ExperimentalPagingApi::class)
class InventoryRemoteMediator(
    private val apiService: APIService,
    private val itemDao: ItemDao
) : RemoteMediator<Int, ItemEntity>() {

    private var currentPage = 1

    override suspend fun load(
        loadType: LoadType,
        state: PagingState<Int, ItemEntity>
    ): MediatorResult {
        val page = when (loadType) {
            LoadType.REFRESH -> 1
            LoadType.PREPEND -> return MediatorResult.Success(endOfPaginationReached = true)
            LoadType.APPEND -> currentPage + 1
        }

        return try {
            val items = apiService.items(offset = page)
            val endOfPaginationReached = items.isEmpty()

            if (loadType == LoadType.REFRESH) {
                currentPage = page
                itemDao.addItem(items.toEntity())
            }

            MediatorResult.Success(endOfPaginationReached = endOfPaginationReached)
        } catch (e: Exception) {
            e.printStackTrace()
            MediatorResult.Error(e)
        }
    }
}