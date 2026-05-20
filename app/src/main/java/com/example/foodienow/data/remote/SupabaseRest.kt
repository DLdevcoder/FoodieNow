package com.example.foodienow.data.remote

import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import java.net.URLEncoder
import java.nio.charset.StandardCharsets

internal data class SupabaseHttpResponse(
    val statusCode: Int,
    val body: String
) {
    val isSuccess: Boolean = statusCode in 200..299
}

internal object SupabaseRest {
    private const val SUPABASE_URL = "https://ruyrncmsawymsrvsluae.supabase.co"
    private const val SUPABASE_KEY = "sb_publishable_vhz-9WFDhqe8ieYKif16dQ_CC5RAcLP"

    fun get(path: String, accessToken: String? = null): SupabaseHttpResponse {
        return request(
            method = "GET",
            path = path,
            body = null,
            accessToken = accessToken,
            prefer = null
        )
    }

    fun post(
        path: String,
        body: JSONObject,
        accessToken: String? = null,
        prefer: String? = null
    ): SupabaseHttpResponse {
        return request(
            method = "POST",
            path = path,
            body = body,
            accessToken = accessToken,
            prefer = prefer
        )
    }

    fun encodeQueryValue(value: String): String {
        return URLEncoder.encode(value, StandardCharsets.UTF_8.toString())
            .replace("+", "%20")
    }

    fun parseErrorMessage(body: String): String {
        if (body.isBlank()) return "Khong the ket noi den Supabase."

        return runCatching {
            val json = JSONObject(body)
            json.optString("message")
                .ifBlank { json.optString("msg") }
                .ifBlank { json.optString("error_description") }
                .ifBlank { json.optString("error") }
                .ifBlank { body }
        }.getOrElse { body }
    }

    private fun request(
        method: String,
        path: String,
        body: JSONObject?,
        accessToken: String?,
        prefer: String?
    ): SupabaseHttpResponse {
        val connection = URL("$SUPABASE_URL$path").openConnection() as HttpURLConnection
        connection.requestMethod = method
        connection.setRequestProperty("apikey", SUPABASE_KEY)
        connection.setRequestProperty("Authorization", "Bearer ${accessToken ?: SUPABASE_KEY}")
        connection.setRequestProperty("Accept", "application/json")
        prefer?.let { connection.setRequestProperty("Prefer", it) }

        if (body != null) {
            connection.doOutput = true
            connection.setRequestProperty("Content-Type", "application/json")
            OutputStreamWriter(connection.outputStream).use { writer ->
                writer.write(body.toString())
                writer.flush()
            }
        }

        val statusCode = connection.responseCode
        val responseStream = if (statusCode in 200..299) {
            connection.inputStream
        } else {
            connection.errorStream
        }

        return SupabaseHttpResponse(
            statusCode = statusCode,
            body = readStream(responseStream)
        )
    }

    private fun readStream(stream: java.io.InputStream?): String {
        if (stream == null) return ""
        return BufferedReader(stream.reader()).use { it.readText() }
    }
}
