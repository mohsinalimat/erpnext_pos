package com.erpnext.pos.utils

import kotlinx.serialization.json.Json
import kotlinx.serialization.json.contentOrNull
import kotlinx.serialization.json.jsonObject
import kotlinx.serialization.json.jsonPrimitive
import kotlin.io.encoding.Base64
import kotlin.io.encoding.ExperimentalEncodingApi
import kotlin.time.Clock
import kotlin.time.ExperimentalTime

@OptIn(ExperimentalEncodingApi::class, ExperimentalTime::class)
object TokenUtils {

    @OptIn(ExperimentalEncodingApi::class)
    fun decodePayload(token: String): Map<String, Any?>? {
        val parts = token.split(".")
        if (parts.size < 2) return null

        return try {
            val payloadJson = Base64.UrlSafe.decode(parts[1]).decodeToString()
            val json = Json.parseToJsonElement(payloadJson).jsonObject

            json.mapValues { it.value.jsonPrimitive.contentOrNull }
        } catch (_: Exception) {
            null
        }
    }

    fun isExpired(token: String?): Boolean {
        if (token == null) return true
        val claims = decodePayload(token) ?: return true

        val exp = claims["exp"]?.toString()?.toLongOrNull() ?: return true
        val now = Clock.System.now().epochSeconds

        return now >= exp
    }

    fun isValid(token: String?): Boolean = token != null && !isExpired(token)
}