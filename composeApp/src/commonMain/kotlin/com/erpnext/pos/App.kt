package com.erpnext.pos

import AppTheme
import androidx.compose.material3.*
import androidx.compose.runtime.Composable
import com.erpnext.pos.di.initKoin

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun App() {
    initKoin()
    AppTheme {
        AppNavigation()
    }
}
