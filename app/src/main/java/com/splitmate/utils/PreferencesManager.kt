package com.splitmate.utils

import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.*
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map
import javax.inject.Inject
import javax.inject.Singleton

@Singleton
class PreferencesManager @Inject constructor(
    private val dataStore: DataStore<Preferences>
) {
    companion object {
        val DARK_MODE = booleanPreferencesKey("dark_mode")
        val CURRENT_USER_ID = stringPreferencesKey("current_user_id")
        val DEFAULT_CURRENCY = stringPreferencesKey("default_currency")
        val ONBOARDING_COMPLETE = booleanPreferencesKey("onboarding_complete")
        val NOTIFICATION_ENABLED = booleanPreferencesKey("notification_enabled")
    }

    val darkMode: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[DARK_MODE] ?: false
    }

    val currentUserId: Flow<String?> = dataStore.data.map { prefs ->
        prefs[CURRENT_USER_ID]
    }

    val defaultCurrency: Flow<String> = dataStore.data.map { prefs ->
        prefs[DEFAULT_CURRENCY] ?: "INR"
    }

    val onboardingComplete: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[ONBOARDING_COMPLETE] ?: false
    }

    val notificationEnabled: Flow<Boolean> = dataStore.data.map { prefs ->
        prefs[NOTIFICATION_ENABLED] ?: true
    }

    suspend fun setDarkMode(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[DARK_MODE] = enabled }
    }

    suspend fun setCurrentUserId(userId: String) {
        dataStore.edit { prefs -> prefs[CURRENT_USER_ID] = userId }
    }

    suspend fun setDefaultCurrency(currency: String) {
        dataStore.edit { prefs -> prefs[DEFAULT_CURRENCY] = currency }
    }

    suspend fun setOnboardingComplete() {
        dataStore.edit { prefs -> prefs[ONBOARDING_COMPLETE] = true }
    }

    suspend fun setNotificationEnabled(enabled: Boolean) {
        dataStore.edit { prefs -> prefs[NOTIFICATION_ENABLED] = enabled }
    }
}
