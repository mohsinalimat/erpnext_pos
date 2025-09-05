package com.erpnext.pos

import android.content.Context
import com.erpnext.pos.remoteSource.oauth.TokenStore
import com.erpnext.pos.remoteSource.oauth.TransientAuthStore
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.sync.Mutex
import androidx.security.crypto.EncryptedSharedPreferences
import kotlinx.coroutines.flow.asStateFlow
import kotlinx.coroutines.sync.withLock
import androidx.core.content.edit
import androidx.security.crypto.MasterKey
import com.erpnext.pos.remoteSource.dto.LoginInfo
import com.erpnext.pos.remoteSource.dto.TokenResponse
import com.erpnext.pos.remoteSource.oauth.AuthInfoStore
import com.erpnext.pos.remoteSource.oauth.SiteStore
import com.erpnext.pos.views.login.Site
import kotlinx.serialization.json.Json

class AndroidTokenStore(private val context: Context) : TokenStore, TransientAuthStore,
    AuthInfoStore, SiteStore {

    private val mutex = Mutex()
    private val stateFlow = MutableStateFlow<TokenResponse?>(null)
    private val json = Json { ignoreUnknownKeys = true }

    private val prefs by lazy {
        val keyGen = MasterKey.Builder(context)
            .setKeyScheme(MasterKey.KeyScheme.AES256_GCM)
            .build()
        EncryptedSharedPreferences.create(
            context,
            "secure_prefs",
            keyGen,
            EncryptedSharedPreferences.PrefKeyEncryptionScheme.AES256_SIV,
            EncryptedSharedPreferences.PrefValueEncryptionScheme.AES256_GCM
        )
    }

    private fun stringKey(key: String) = key

    override suspend fun save(tokens: TokenResponse) {
        clear()
        prefs.edit {
            putString(stringKey("access_token"), tokens.access_token)
            putString(stringKey("refresh_token"), tokens.refresh_token)
            putString(stringKey("id_token"), tokens.id_token)
            putLong(stringKey("expires_in"), tokens.expires_in ?: 0L)
            apply()
        }
        stateFlow.value = tokens
    }

    override fun load(): TokenResponse? {
        val at = prefs.getString(stringKey("access_token"), null) ?: return null
        val rt = prefs.getString(stringKey("refresh_token"), null) ?: ""
        val expires = prefs.getLong(stringKey("expires"), 0L)
        val idToken = prefs.getString(stringKey("id_token"), null) ?: return null
        val tokens = TokenResponse(
            access_token = at,
            refresh_token = rt,
            expires_in = expires,
            id_token = idToken
        )
        stateFlow.value = tokens
        return tokens
    }

    override suspend fun loadAuthInfo(): LoginInfo {
        val url = prefs.getString(stringKey("base_url"), null) ?: ""
        val clientId = prefs.getString(stringKey("client_id"), null) ?: ""
        val clientSecret = prefs.getString(stringKey("client_secret"), null) ?: ""
        val redirectUrl = prefs.getString(stringKey("redirect_url"), null) ?: ""
        return LoginInfo(
            url = url,
            clientSecret = clientSecret,
            clientId = clientId,
            redirectUrl = redirectUrl,
            scopes = listOf("all", "openid")
        )
    }

    override suspend fun saveAuthInfo(info: LoginInfo) {
        clear()
        prefs.edit().apply {
            putString(stringKey("base_url"), info.url)
            putString(stringKey("client_id"), info.clientId)
            putString(stringKey("client_secret"), info.clientSecret)
            putString(stringKey("redirect_url"), info.redirectUrl)
            apply()
        }
    }

    override suspend fun clear() = mutex.withLock {
        prefs.edit { clear() }
        stateFlow.value = null
    }

    override fun tokensFlow() = stateFlow.asStateFlow()

    // TransientAuthStore
    override suspend fun savePkceVerifier(verifier: String) {
        prefs.edit { putString("pkce_verifier", verifier) }
    }

    override suspend fun loadPkceVerifier(): String? = prefs.getString("pkce_verifier", null)
    override suspend fun clearPkceVerifier() {
        prefs.edit { remove("pkce_verifier") }
    }

    override suspend fun saveState(state: String) {
        prefs.edit { putString("oauth_state", state) }
    }

    override suspend fun loadState(): String? = prefs.getString("oauth_state", null)
    override suspend fun clearState() {
        prefs.edit { remove("oauth_state") }
    }

    // Site Store
    override fun loadSites(): MutableList<Site>? {
        val sites = prefs.getString("sites", null) ?: return mutableListOf<Site>()
        return json.decodeFromString(sites)
    }

    override suspend fun saveSite(site: Site) {
        val list = loadSites()
        list?.add(site)
        val serialized = json.encodeToString(list)
        prefs.edit().apply {
            putString("sites", serialized)
            apply()
        }
    }

    override suspend fun clearSite(url: String) {
        val sites = loadSites() ?: emptyList()
        val updated = sites.filter { it.url != url }
        updated.forEach { saveSite(it) }
    }
}