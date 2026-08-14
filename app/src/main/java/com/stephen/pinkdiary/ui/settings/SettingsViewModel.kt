package com.stephen.pinkdiary.ui.settings

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.stephen.pinkdiary.PinkdiaryApp
import com.stephen.pinkdiary.data.model.UserSettings
import com.stephen.pinkdiary.data.repository.UserSettingsRepository
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch

class SettingsViewModel(
    private val settingsRepository: UserSettingsRepository
) : ViewModel() {

    val settings: StateFlow<UserSettings> = settingsRepository.settings
        .stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), UserSettings())

    fun update(settings: UserSettings) {
        viewModelScope.launch {
            settingsRepository.update(settings)
        }
    }

    companion object {
        fun factory(app: PinkdiaryApp): ViewModelProvider.Factory = viewModelFactory {
            initializer { SettingsViewModel(app.userSettingsRepository) }
        }
    }
}
