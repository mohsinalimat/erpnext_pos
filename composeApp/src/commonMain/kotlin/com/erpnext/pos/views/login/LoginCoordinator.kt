package com.erpnext.pos.views.login

import androidx.compose.runtime.Composable
import androidx.compose.runtime.remember
import com.erpnext.pos.remoteSource.dto.TokenResponse
import io.ktor.http.Url
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.runBlocking
import kotlinx.coroutines.withContext
import org.koin.compose.koinInject
import org.koin.core.option.viewModelScopeFactory

class LoginCoordinator(
    val viewModel: LoginViewModel
) {
    val screenStateFlow = viewModel.stateFlow

    fun existingSites(): List<Site>? {
        return viewModel.existingSites()
    }

    fun onSiteSelected(site: Site) {
        return viewModel.onSiteSelected(site)
    }

    fun onAddSite(site: String) {
        val url =
            Url(site).host.split(".")[0].replaceFirstChar { if (it.isLowerCase()) it.titlecase() else it.toString() }
        val site = Site(url = site, name = url)
        viewModel.onAddSite(site)
    }

    fun onError(error: String) {
        viewModel.onError(error)
    }

    fun onReset() {
        viewModel.reset()
    }

    fun isAuthenticated(tokens: TokenResponse) {
        viewModel.isAuthenticated(tokens)
    }
}

@Composable
fun rememberLoginCoordinator(): LoginCoordinator {
    val viewModel: LoginViewModel = koinInject()

    return remember(viewModel) {
        LoginCoordinator(
            viewModel = viewModel
        )
    }
}