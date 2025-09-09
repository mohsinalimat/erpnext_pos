package com.erpnext.pos.localSource.dao

import androidx.paging.PagingSource
import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.erpnext.pos.localSource.entities.ItemEntity
import kotlinx.coroutines.flow.Flow

@Dao
interface ItemDao {
    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addItem(items: List<ItemEntity>)

    @Query("SELECT * FROM tabItem")
    fun getAllItems(): PagingSource<Int, ItemEntity>

    @Query("SELECT * FROM tabItem WHERE name = :itemId")
    fun getCategory(itemId: String): ItemEntity

    @Query("DELETE FROM tabItem")
    suspend fun deleteAll()
}