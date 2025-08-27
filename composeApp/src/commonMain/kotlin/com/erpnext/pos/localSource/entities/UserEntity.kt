package com.erpnext.pos.localSource.entities

import androidx.room.Entity
import androidx.room.PrimaryKey

@Entity(tableName = "tabUser")
data class UserEntity(
    @PrimaryKey(autoGenerate = false)
    val name: String,
    val firstName: String,
    val lastName: String?,
    val username: String?,

    val email: String,
    val mobileNo: String?,

    val deskTheme: String = "",

    val enabled: Boolean
)