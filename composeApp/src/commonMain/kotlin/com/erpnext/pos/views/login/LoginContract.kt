package com.erpnext.pos.views.login

import com.erpnext.pos.remoteSource.dto.TokenResponse
import kotlinx.serialization.Serializable

sealed class LoginState {
    object Loading : LoginState()
    object Success : LoginState()
    data class Authenticated(val tokens: TokenResponse) : LoginState()
    data class Error(val message: String) : LoginState()
}

data class LoginAction(
    val existingSites: () -> List<Site>? = { emptyList() },
    val onSiteSelected: (site: Site) -> Unit = { },
    val onAddSite: (String) -> Unit = {},
    val isAuthenticated: (TokenResponse) -> Unit = { },
    val onError: (error: String) -> Unit = {},
    val onReset: () -> Unit = {}
)

@Serializable
data class Site(val url: String, val name: String)