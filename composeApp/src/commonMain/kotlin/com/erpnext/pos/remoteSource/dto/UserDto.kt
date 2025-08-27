package com.erpnext.pos.remoteSource.dto

data class UserDto(
    val name: String,
    val username: String,
    val firstName: String,
    val lastName: String,
    val email: String,
    val mobileNo: String,
    val salt: String,
    val hash: String,
    val enabled: Boolean
)