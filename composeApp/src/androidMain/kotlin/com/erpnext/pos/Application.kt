package com.erpnext.pos

import android.app.Application
import com.erpnext.pos.di.initKoin
import org.koin.android.ext.koin.androidContext

class Application : Application() {

    override fun onCreate() {
        super.onCreate()
        AppContext.init(this)

        initKoin({ androidContext(this@Application) }, listOf(androidModule))
    }
}