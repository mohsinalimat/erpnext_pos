package com.erpnext.pos.data.repositories

import com.erpnext.pos.domain.models.UserBO
import com.erpnext.pos.domain.repositories.ILoginRepository
import com.erpnext.pos.remoteSource.datasources.LoginRemoteSource
import com.erpnext.pos.remoteSource.dto.CredentialsDto
import com.erpnext.pos.data.mappers.toBO

class LoginRepositories(
    private val remoteSource: LoginRemoteSource
): ILoginRepository {
    override suspend fun login(credentials: CredentialsDto): UserBO {
        return remoteSource.login(credentials).toBO()
    }
}