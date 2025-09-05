package com.erpnext.pos.remoteSource.dto

data class LoginInfo(
    val url: String,
    val redirectUrl: String,
    val clientId: String,
    val clientSecret: String,
    val scopes: List<String>
)