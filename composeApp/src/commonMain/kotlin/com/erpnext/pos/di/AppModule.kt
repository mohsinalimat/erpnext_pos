package com.erpnext.pos.di

import androidx.datastore.preferences.core.PreferenceDataStoreFactory
import com.erpnext.pos.data.AppPreferences
import com.erpnext.pos.data.repositories.LoginRepositories
import com.erpnext.pos.domain.usecases.LoginUseCase
import com.erpnext.pos.navigation.NavigationManager
import com.erpnext.pos.remoteSource.api.APIService
import com.erpnext.pos.remoteSource.datasources.LoginRemoteSource
import com.erpnext.pos.views.login.LoginViewModel
import com.erpnext.pos.views.splash.SplashViewModel
import io.ktor.client.*
import kotlinx.coroutines.CoroutineScope
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.IO
import kotlinx.coroutines.SupervisorJob
import kotlinx.io.files.SystemFileSystem
import okio.Path
import okio.Path.Companion.toPath
import org.koin.core.context.startKoin
import org.koin.dsl.KoinAppDeclaration
import org.koin.dsl.module

val appModule = module {

    single { HttpClient () }
    single {
        APIService(
            client = get(),
            baseUrl = "https://erp-ni.distribuidorareyes.com/"
        )
    }

    single { LoginRemoteSource(get()) }
    single { LoginRepositories(get()) }
    single { LoginUseCase(get()) }
    single { LoginViewModel(get()) }
    single { AppPreferences(get()) }
    single<CoroutineScope> { CoroutineScope(SupervisorJob() + Dispatchers.IO) }
    single { NavigationManager(get()) }
    single { SplashViewModel(get(), get()) }
    single {
        PreferenceDataStoreFactory.createWithPath {
            "./prefs.preferences_pb".toPath()
        }
    }
}

fun initKoin(config: KoinAppDeclaration? = null) {
    startKoin {
        config?.invoke(this)
        modules(appModule)
    }
}