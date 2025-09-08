package com.erpnext.pos.data.repositories

import com.erpnext.pos.data.mappers.toBO
import com.erpnext.pos.domain.models.CategoryBO
import com.erpnext.pos.domain.models.ItemBO
import com.erpnext.pos.domain.repositories.IInventoryRepository
import com.erpnext.pos.remoteSource.datasources.InventoryRemoteSource

class InventoryRepository(
    private val remoteSource: InventoryRemoteSource
) : IInventoryRepository {

    override suspend fun getItems(): List<ItemBO> {
        return remoteSource.getItems().toBO()
    }

    override suspend fun getItemDetails(itemId: String): ItemBO {
        return remoteSource.getItemDetail(itemId).toBO()
    }

    override suspend fun getCategories(): List<CategoryBO> {
        return remoteSource.getCategories().toBO()
    }
}