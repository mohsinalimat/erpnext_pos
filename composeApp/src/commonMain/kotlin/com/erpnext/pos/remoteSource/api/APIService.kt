package com.erpnext.pos.remoteSource.api

import com.erpnext.pos.remoteSource.dto.CredentialsDto
import com.erpnext.pos.remoteSource.dto.UserDto
import io.ktor.client.*
import io.ktor.client.call.*
import io.ktor.client.request.*
import io.ktor.http.*


class APIService(
    private val client: HttpClient,
    private val baseUrl: String
) {

    suspend fun login(credentials: CredentialsDto): UserDto {
        return client.post("$baseUrl/login") {
            contentType(ContentType.Application.Json)
            setBody(credentials)
        }.body()
    }
}
