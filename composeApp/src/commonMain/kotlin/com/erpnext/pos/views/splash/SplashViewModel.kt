package com.erpnext.pos.views.splash

import com.erpnext.pos.base.BaseViewModel
import com.erpnext.pos.navigation.NavRoute
import com.erpnext.pos.navigation.NavigationManager
import com.erpnext.pos.remoteSource.oauth.TokenStore
import com.erpnext.pos.utils.TokenUtils
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.flow.update

class SplashViewModel(
    private val navigationManager: NavigationManager,
    private val tokenStore: TokenStore
) : BaseViewModel() {

    private val _stateFlow: MutableStateFlow<SplashState> = MutableStateFlow(SplashState.Loading)
    val stateFlow: StateFlow<SplashState> = _stateFlow.asStateFlow()

    fun verifyToken() {
        _stateFlow.update { SplashState.Loading }
        val tokens = tokenStore.load()
        tokens.let { token ->
            if (TokenUtils.isValid(token?.id_token))
                navigationManager.navigateTo(NavRoute.Home)
            else navigationManager.navigateTo(NavRoute.Login)
            _stateFlow.update { SplashState.Success }
        }
    }
}