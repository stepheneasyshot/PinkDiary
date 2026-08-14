package com.stephen.pinkdiary.ui.settings

import androidx.annotation.StringRes
import com.stephen.pinkdiary.data.model.UserSettings

data class SettingsUiState(
    val settings: UserSettings = UserSettings(),
    val isLoading: Boolean = true
)

sealed interface SettingsIntent {
    data class DefaultCycleLengthChanged(val value: Int) : SettingsIntent
    data class DefaultPeriodLengthChanged(val value: Int) : SettingsIntent
    data class RecentCyclesChanged(val value: Int) : SettingsIntent
}

sealed interface SettingsEffect {
    data class ShowMessage(@param:StringRes val messageRes: Int) : SettingsEffect
}
