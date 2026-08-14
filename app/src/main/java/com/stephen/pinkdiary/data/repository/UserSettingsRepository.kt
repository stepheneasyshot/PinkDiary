package com.stephen.pinkdiary.data.repository

import android.content.Context
import androidx.datastore.core.DataStore
import androidx.datastore.preferences.core.Preferences
import androidx.datastore.preferences.core.edit
import androidx.datastore.preferences.core.booleanPreferencesKey
import androidx.datastore.preferences.core.intPreferencesKey
import androidx.datastore.preferences.preferencesDataStore
import com.stephen.pinkdiary.data.model.UserSettings
import kotlinx.coroutines.flow.Flow
import kotlinx.coroutines.flow.map

private val Context.dataStore: DataStore<Preferences> by preferencesDataStore(name = "settings")

class UserSettingsRepository(context: Context) {

    private val appContext = context.applicationContext

    private object Keys {
        val CYCLE_LENGTH = intPreferencesKey("default_cycle_length")
        val PERIOD_LENGTH = intPreferencesKey("default_period_length")
        val RECENT_N = intPreferencesKey("recent_n")
        val ONBOARDING_COMPLETED = booleanPreferencesKey("onboarding_completed")
    }

    val settings: Flow<UserSettings> = appContext.dataStore.data.map { prefs ->
        val defaults = UserSettings()
        UserSettings(
            defaultCycleLength = prefs[Keys.CYCLE_LENGTH] ?: defaults.defaultCycleLength,
            defaultPeriodLength = prefs[Keys.PERIOD_LENGTH] ?: defaults.defaultPeriodLength,
            recentN = prefs[Keys.RECENT_N] ?: defaults.recentN
        )
    }

    suspend fun update(settings: UserSettings) {
        appContext.dataStore.edit { prefs ->
            prefs[Keys.CYCLE_LENGTH] = settings.defaultCycleLength
            prefs[Keys.PERIOD_LENGTH] = settings.defaultPeriodLength
            prefs[Keys.RECENT_N] = settings.recentN
        }
    }

    /** 是否已完成首次启动的引导 */
    val onboardingCompleted: Flow<Boolean> = appContext.dataStore.data.map { prefs ->
        prefs[Keys.ONBOARDING_COMPLETED] ?: false
    }

    suspend fun setOnboardingCompleted() {
        appContext.dataStore.edit { prefs ->
            prefs[Keys.ONBOARDING_COMPLETED] = true
        }
    }
}
