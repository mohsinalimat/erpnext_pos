package com.erpnext.pos.remoteSource.datasources

import com.erpnext.pos.localSource.dao.ItemDao
import com.erpnext.pos.remoteSource.api.APIService
import com.erpnext.pos.remoteSource.dto.CategoryDto
import com.erpnext.pos.remoteSource.dto.ItemDto

import com.erpnext.pos.remoteSource.mapper.toEntity

class InventoryRemoteSource(
    private val apiService: APIService,
    private val itemDao: ItemDao,
) {
    suspend fun getItems(): List<ItemDto> {
        val items = apiService.items()
        itemDao.addItem(items.toEntity())

        return items
    }

    suspend fun getItemDetail(itemId: String): ItemDto {
        return apiService.getItemDetail(itemId)
    }

    suspend fun getCategories(): List<CategoryDto> {
        return apiService.getCategories()
    }

}