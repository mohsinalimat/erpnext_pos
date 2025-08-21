package com.erpnext.pos.data.local.dao

import androidx.room.Dao
import androidx.room.Insert
import androidx.room.OnConflictStrategy
import androidx.room.Query
import com.erpnext.pos.data.local.entities.UserEntity

@Dao
interface UserDao {

    @Insert(onConflict = OnConflictStrategy.REPLACE)
    suspend fun addUser(categories: UserEntity)

    @Query("SELECT * FROM tabUser")
    suspend fun getUser(): UserEntity

    @Query("DELETE FROM tabUser")
    suspend fun deleteAllUsers()
}