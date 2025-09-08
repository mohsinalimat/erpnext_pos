package com.erpnext.pos.domain.usecases

import com.erpnext.pos.data.repositories.InventoryRepository
import com.erpnext.pos.domain.models.CategoryBO

class FetchCategoriesUseCase(
    private val repo: InventoryRepository
) : UseCase<Unit, List<CategoryBO>>() {
    override suspend fun useCaseFunction(input: Unit): List<CategoryBO> {
        return repo.getCategories()
    }
}