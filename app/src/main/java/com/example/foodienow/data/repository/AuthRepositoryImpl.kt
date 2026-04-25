package com.example.foodienow.data.repository

import com.example.foodienow.data.local.AuthSessionDataStore
import com.example.foodienow.domain.model.User
import com.example.foodienow.domain.model.UserRole
import com.example.foodienow.domain.repository.AuthRepository
import kotlinx.coroutines.Dispatchers
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.withContext
import org.json.JSONObject
import java.io.BufferedReader
import java.io.OutputStreamWriter
import java.net.HttpURLConnection
import java.net.URL
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class AuthRepositoryImpl @Inject constructor(
    private val authSessionDataStore: AuthSessionDataStore
) : AuthRepository {

    override suspend fun login(email: String, pass: String): Result<User> = withContext(Dispatchers.IO) {
        try {
            val response = postRequest(
                endpoint = "/auth/v1/token?grant_type=password",
                body = JSONObject()
                    .put("email", email)
                    .put("password", pass)
            )

            if (!response.isSuccess) {
                return@withContext Result.failure(Exception(parseErrorMessage(response.body)))
            }

            val token = response.body.optString("access_token")
            val userJson = response.body.optJSONObject("user") ?: JSONObject()
            val isEmailConfirmed = userJson.optString("email_confirmed_at").isNotBlank()
            if (!isEmailConfirmed) {
                return@withContext Result.failure(
                    Exception("Tai khoan chua xac thuc email. Vui long kiem tra hop thu.")
                )
            }

            val user = userJson.toDomainUser(token)
            authSessionDataStore.saveSession(user)
            Result.success(user)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun register(email: String, pass: String, role: UserRole): Result<User> = withContext(Dispatchers.IO) {
        try {
            val metadata = JSONObject().put("role", role.name)

            val response = postRequest(
                endpoint = "/auth/v1/signup",
                body = JSONObject()
                    .put("email", email)
                    .put("password", pass)
                    .put("data", metadata)
            )

            if (!response.isSuccess) {
                return@withContext Result.failure(Exception(parseErrorMessage(response.body)))
            }

            val userJson = response.body.optJSONObject("user") ?: JSONObject()
            val token = response.body.optString("access_token")
            Result.success(userJson.toDomainUser(token))
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun resendVerificationEmail(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = postRequest(
                endpoint = "/auth/v1/resend",
                body = JSONObject()
                    .put("type", "signup")
                    .put("email", email)
            )

            if (!response.isSuccess) {
                return@withContext Result.failure(Exception(parseErrorMessage(response.body)))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun forgotPassword(email: String): Result<Unit> = withContext(Dispatchers.IO) {
        try {
            val response = postRequest(
                endpoint = "/auth/v1/recover",
                body = JSONObject().put("email", email)
            )

            if (!response.isSuccess) {
                return@withContext Result.failure(Exception(parseErrorMessage(response.body)))
            }

            Result.success(Unit)
        } catch (e: Exception) {
            Result.failure(e)
        }
    }

    override suspend fun logout(): Result<Unit> = withContext(Dispatchers.IO) {
        authSessionDataStore.clearSession()
        Result.success(Unit)
    }

    override fun getAuthState(): Flow<User?> = authSessionDataStore.sessionFlow

    private fun postRequest(endpoint: String, body: JSONObject): HttpResponseData {
        val connection = URL("$SUPABASE_URL$endpoint").openConnection() as HttpURLConnection
        connection.requestMethod = "POST"
        connection.doOutput = true
        connection.setRequestProperty("apikey", SUPABASE_KEY)
        connection.setRequestProperty("Authorization", "Bearer $SUPABASE_KEY")
        connection.setRequestProperty("Content-Type", "application/json")

        OutputStreamWriter(connection.outputStream).use { writer ->
            writer.write(body.toString())
            writer.flush()
        }

        val statusCode = connection.responseCode
        val responseStream = if (statusCode in 200..299) connection.inputStream else connection.errorStream
        val payload = readStream(responseStream)
        return HttpResponseData(
            statusCode = statusCode,
            body = if (payload.isBlank()) JSONObject() else JSONObject(payload)
        )
    }

    private fun readStream(stream: java.io.InputStream?): String {
        if (stream == null) return ""
        return BufferedReader(stream.reader()).use { it.readText() }
    }

    private fun parseErrorMessage(body: JSONObject): String {
        return body.optString("msg")
            .ifBlank { body.optString("error_description") }
            .ifBlank { body.optString("error") }
            .ifBlank { "Khong the ket noi den he thong xac thuc." }
    }

    private fun JSONObject.toDomainUser(token: String): User {
        val metadata = optJSONObject("user_metadata") ?: JSONObject()
        val role = metadata.optString("role")
            .toUserRoleOrDefault()

        return User(
            id = optString("id"),
            name = metadata.optString("name").ifBlank { optString("email").substringBefore("@") },
            email = optString("email"),
            role = role,
            token = token
        )
    }

    private fun String.toUserRoleOrDefault(): UserRole {
        return runCatching { UserRole.valueOf(this.uppercase()) }
            .getOrDefault(UserRole.CUSTOMER)
    }

    private data class HttpResponseData(
        val statusCode: Int,
        val body: JSONObject
    ) {
        val isSuccess: Boolean = statusCode in 200..299
    }

    companion object {
        private const val SUPABASE_URL = "https://ruyrncmsawymsrvsluae.supabase.co"
        private const val SUPABASE_KEY = "sb_publishable_vhz-9WFDhqe8ieYKif16dQ_CC5RAcLP"
    }
}

