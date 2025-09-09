package com.erpnext.pos.remoteSource.api

import io.ktor.client.HttpClient
import io.ktor.client.request.get
import io.ktor.client.statement.HttpResponse
import kotlinx.serialization.json.Json

import io.ktor.client.request.*
import io.ktor.client.statement.*
import io.ktor.http.*
import kotlinx.serialization.Serializable
import kotlinx.serialization.json.*

suspend inline fun <reified T> HttpClient.getERPList(
    doctype: String,
    fields: List<String> = emptyList(),
    filters: List<List<Any>> = emptyList(),
    limit: Int = 20,
    offset: Int = 0,
    orderBy: String? = null,
    orderType: String = "desc",
    baseUrl: String?
): List<T> {
    if (baseUrl.isNullOrEmpty()) {
        throw IllegalArgumentException("Petición inválida: baseUrl no puede ser nulo o vacío")
    }

    val queryParams = Parameters.build {
        if (fields.isNotEmpty()) {
            append("fields", Json.encodeToString(fields))
        }

        if (filters.isNotEmpty()) {
            // Convertimos List<List<Any>> a un string JSON manualmente o con JsonArray
            val jsonFiltersArray = buildJsonArray {
                filters.forEach { filterCondition -> // filterCondition es List<Any> de tipo [String, String, Any]
                    if (filterCondition.size == 3) {
                        val field = filterCondition[0] as String
                        val operator = filterCondition[1] as String
                        val value = filterCondition[2] // Este es el 'Any'

                        add(buildJsonArray {
                            add(JsonPrimitive(field))
                            add(JsonPrimitive(operator))
                            // Ahora manejamos el 'value' de forma explícita
                            when (value) {
                                is String -> add(JsonPrimitive(value))
                                is Number -> add(JsonPrimitive(value))
                                is Boolean -> add(JsonPrimitive(value))
                                // Si tu DSL puede generar listas para operadores como "in"
                                is List<*> -> {
                                    add(buildJsonArray {
                                        value.forEach { listItem ->
                                            when (listItem) {
                                                is String -> add(JsonPrimitive(listItem))
                                                is Number -> add(JsonPrimitive(listItem))
                                                is Boolean -> add(JsonPrimitive(listItem))
                                                else -> throw IllegalArgumentException(
                                                    "Tipo no soportado en lista de valor de filtro: ${listItem?.let { it::class.simpleName }}"
                                                )
                                            }
                                        }
                                    })
                                }

                                else -> throw IllegalArgumentException(
                                    "Tipo de valor no soportado en filtro DSL: ${value::class.simpleName}. Campo: $field"
                                )
                            }
                        })
                    } else {
                        // Opcional: manejar condiciones de filtro mal formadas
                        println("Advertencia: Condición de filtro mal formada ignorada: $filterCondition")
                    }
                }
            }
            append("filters", jsonFiltersArray.toString())
        }

        append("limit_page_length", limit.toString())
        append("limit_start", offset.toString())
        orderBy?.let {
            append("order_by", it)
            append("order_type", orderType) // Asumo que order_type siempre acompaña a order_by
        }
    }

    val urlBuilder = URLBuilder(baseUrl)
    urlBuilder.appendPathSegments("api", "resource", doctype)
    urlBuilder.parameters.appendAll(queryParams)

    val url: Url = urlBuilder.build()

    println("Requesting URL: $url") // Para depuración

    val response: HttpResponse = this.get(url) // HttpClient viene de la extensión
    val responseBodyText = response.bodyAsText()

    println("Response status: ${response.status}")
    println("Response body: $responseBodyText") // Para depuración

    if (!response.status.isSuccess()) {
        // Intenta parsear el error de Frappe si es posible
        try {
            val errorResponse = Json.decodeFromString<FrappeErrorResponse>(responseBodyText)
            throw FrappeException(
                errorResponse.exception ?: "Error en la petición: ${response.status}", errorResponse
            )
        } catch (e: Exception) { // kotlinx.serialization.SerializationException u otros
            throw Exception("Error en la petición: ${response.status} - $responseBodyText", e)
        }
    }

    val jsonResponse = Json.parseToJsonElement(responseBodyText).jsonObject
    val dataElement = jsonResponse["data"]
        ?: throw FrappeException("La respuesta JSON no contiene la clave 'data'. Respuesta: $responseBodyText")

    // Ahora decodificamos el JsonElement que está bajo "data"
    return Json { ignoreUnknownKeys = true }.decodeFromJsonElement(dataElement)
}

// Clases para manejar errores de Frappe (opcional pero recomendado)
@Serializable
data class FrappeErrorResponse(
    val exception: String? = null,
    val _server_messages: String? = null,
)

class FrappeException(message: String, val errorResponse: FrappeErrorResponse? = null) :
    Exception(message)

suspend inline fun <reified T> HttpClient.getERPSingle(
    doctype: String,
    name: String,
    baseUrl: String
): T {
    val url = "$baseUrl/api/resource/$doctype/$name"
    val response: HttpResponse = this.get(url)
    val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
    return Json.decodeFromString(json["data"].toString())
}

suspend inline fun <reified T, reified R> HttpClient.postERP(
    doctype: String,
    payload: T,
    baseUrl: String
): R {
    val url = "$baseUrl/api/resource/$doctype"
    val response: HttpResponse = this.post(url) {
        setBody(Json.encodeToString(payload))
    }
    val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
    return Json.decodeFromString(json["data"].toString())
}

suspend inline fun <reified T, reified R> HttpClient.putERP(
    doctype: String,
    name: String,
    payload: T,
    baseUrl: String
): R {
    val url = "$baseUrl/api/resource/$doctype/$name"
    val response: HttpResponse = this.put(url) {
        setBody(Json.encodeToString(payload))
    }
    val json = Json.parseToJsonElement(response.bodyAsText()).jsonObject
    return Json.decodeFromString(json["data"].toString())
}

suspend fun HttpClient.deleteERP(
    doctype: String,
    name: String,
    apiKey: String,
    apiSecret: String,
    baseUrl: String
) {
    val url = "$baseUrl/api/resource/$doctype/$name"
    this.delete(url) {
        headers { append("Authorization", "token $apiKey:$apiSecret") }
    }
}
