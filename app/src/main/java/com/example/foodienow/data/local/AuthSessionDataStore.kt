package com.example.foodienow.data.local

import android.content.Context
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.core.longPreferencesKey
import androidx.datastore.preferences.core.stringPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.example.foodienow.domain.model.User
import com.example.foodienow.domain.model.UserRole
import dagger.hilt.android.qualifiers.ApplicationContext
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

private val Context.authSessionDataStore by preferencesDataStore(name = "auth_session")

@Singleton
class AuthSessionDataStore @Inject constructor(
    @param:ApplicationContext private val context: Context
) {
    val sessionFlow: Flow<User?> = context.authSessionDataStore.data.map { prefs ->
        prefs.toUserOrNull()
    }

    suspend fun saveSession(user: User) {
        context.authSessionDataStore.edit { prefs ->
            prefs[Keys.USER_ID] = user.id
            prefs[Keys.NAME] = user.name
            prefs[Keys.EMAIL] = user.email
            prefs[Keys.ROLE] = user.role.name
            prefs[Keys.TOKEN] = user.token
            prefs[Keys.REFRESH_TOKEN] = user.refreshToken
            prefs[Keys.BALANCE] = user.balance
            prefs[Keys.REWARD_POINTS] = user.rewardPoints
        }
    }

    suspend fun clearSession() {
        context.authSessionDataStore.edit { prefs ->
            prefs.remove(Keys.USER_ID)
            prefs.remove(Keys.NAME)
            prefs.remove(Keys.EMAIL)
            prefs.remove(Keys.ROLE)
            prefs.remove(Keys.TOKEN)
            prefs.remove(Keys.REFRESH_TOKEN)
            prefs.remove(Keys.BALANCE)
            prefs.remove(Keys.REWARD_POINTS)
        }
    }

    private fun Preferences.toUserOrNull(): User? {
        val id = this[Keys.USER_ID].orEmpty()
        val email = this[Keys.EMAIL].orEmpty()
        val token = this[Keys.TOKEN].orEmpty()
        val refreshToken = this[Keys.REFRESH_TOKEN].orEmpty()

        if (id.isBlank() || email.isBlank() || token.isBlank()) {
            return null
        }

        val role = runCatching {
            UserRole.valueOf(this[Keys.ROLE].orEmpty().uppercase())
        }.getOrDefault(UserRole.CUSTOMER)

        val name = this[Keys.NAME].orEmpty().ifBlank { email.substringBefore("@") }

        return User(
            id = id,
            name = name,
            email = email,
            role = role,
            balance = this[Keys.BALANCE] ?: 0L,
            rewardPoints = this[Keys.REWARD_POINTS] ?: 0,
            token = token,
            refreshToken = refreshToken
        )
    }

    private object Keys {
        val USER_ID = stringPreferencesKey("user_id")
        val NAME = stringPreferencesKey("name")
        val EMAIL = stringPreferencesKey("email")
        val ROLE = stringPreferencesKey("role")
        val TOKEN = stringPreferencesKey("token")
        val REFRESH_TOKEN = stringPreferencesKey("refresh_token")
        val BALANCE = longPreferencesKey("balance")
        val REWARD_POINTS = intPreferencesKey("reward_points")
    }
}

