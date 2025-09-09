package com.erpnext.pos

import android.app.Application
import com.erpnext.pos.data.AppDatabase
import com.erpnext.pos.data.DatabaseBuilder
import com.erpnext.pos.di.initKoin
import io.ktor.http.parametersOf
import org.koin.android.ext.koin.androidContext

class Application : Application() {

    override fun onCreate() {
        super.onCreate()
        AppContext.init(this)

        initKoin(
            {
                androidContext(this@Application)
            },
            listOf(androidModule),
            builder = DatabaseBuilder(this@Application)
        )
    }
}