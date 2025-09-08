package com.erpnext.pos.domain.usecases

import com.erpnext.pos.data.repositories.InventoryRepository
import com.erpnext.pos.domain.models.ItemBO
import com.erpnext.pos.domain.repositories.IInventoryRepository

class FetchInventoryItemUseCase(
    private val repo: IInventoryRepository
) : UseCase<Unit, List<ItemBO>>() {
    override suspend fun useCaseFunction(input: Unit): List<ItemBO>{
        return repo.getItems()
    }
}