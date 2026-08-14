package com.stephen.pinkdiary.ui.settings

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.stephen.pinkdiary.PinkdiaryApp
import com.stephen.pinkdiary.R
import com.stephen.pinkdiary.data.model.UserSettings
import com.stephen.pinkdiary.data.repository.UserSettingsRepository
import com.stephen.pinkdiary.ui.mvi.MviViewModel
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: UserSettingsRepository
) : MviViewModel<SettingsIntent, SettingsUiState, SettingsEffect>(SettingsUiState()) {

    init {
        viewModelScope.launch {
            settingsRepository.settings.collect { settings ->
                reduce { it.copy(settings = settings, isLoading = false) }
            }
        }
    }

    override fun onIntent(intent: SettingsIntent) {
        val current = uiState.value.settings
        val updated = when (intent) {
            is SettingsIntent.DefaultCycleLengthChanged -> current.copy(
                defaultCycleLength = intent.value.coerceIn(MIN_CYCLE_LENGTH, MAX_CYCLE_LENGTH)
            )
            is SettingsIntent.DefaultPeriodLengthChanged -> current.copy(
                defaultPeriodLength = intent.value.coerceIn(MIN_PERIOD_LENGTH, MAX_PERIOD_LENGTH)
            )
            is SettingsIntent.RecentCyclesChanged -> current.copy(
                recentN = intent.value.coerceIn(MIN_RECENT_CYCLES, MAX_RECENT_CYCLES)
            )
        }
        updateSettings(current = current, updated = updated)
    }

    private fun updateSettings(current: UserSettings, updated: UserSettings) {
        if (current == updated) return
        reduce { it.copy(settings = updated) }
        viewModelScope.launch {
            runCatching { settingsRepository.update(updated) }
                .onFailure {
                    reduce { state -> state.copy(settings = current) }
                    emitEffect(SettingsEffect.ShowMessage(R.string.error_generic))
                }
        }
    }

    companion object {
        const val MIN_CYCLE_LENGTH = 21
        const val MAX_CYCLE_LENGTH = 45
        const val MIN_PERIOD_LENGTH = 2
        const val MAX_PERIOD_LENGTH = 10
        const val MIN_RECENT_CYCLES = 1
        const val MAX_RECENT_CYCLES = 12

        fun factory(app: PinkdiaryApp): ViewModelProvider.Factory = viewModelFactory {
            initializer { SettingsViewModel(app.userSettingsRepository) }
        }
    }
}
