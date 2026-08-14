package com.stephen.pinkdiary.ui.app

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.stephen.pinkdiary.PinkdiaryApp
import com.stephen.pinkdiary.R
import com.stephen.pinkdiary.data.repository.UserSettingsRepository
import com.stephen.pinkdiary.ui.mvi.MviViewModel
import kotlinx.coroutines.flow.collectLatest
import kotlinx.coroutines.launch

class AppViewModel(
    private val settingsRepository: UserSettingsRepository
) : MviViewModel<AppIntent, AppUiState, AppEffect>(AppUiState()) {

    init {
        viewModelScope.launch {
            settingsRepository.onboardingCompleted.collectLatest { completed ->
                reduce {
                    it.copy(
                        destination = if (completed) AppDestination.MAIN else AppDestination.ONBOARDING,
                        isCompletingOnboarding = false
                    )
                }
            }
        }
    }

    override fun onIntent(intent: AppIntent) {
        when (intent) {
            AppIntent.OnboardingFinished -> completeOnboarding()
        }
    }

    private fun completeOnboarding() {
        if (uiState.value.isCompletingOnboarding) return
        reduce { it.copy(isCompletingOnboarding = true) }
        viewModelScope.launch {
            runCatching { settingsRepository.setOnboardingCompleted() }
                .onFailure {
                    reduce { state -> state.copy(isCompletingOnboarding = false) }
                    emitEffect(AppEffect.ShowMessage(R.string.error_generic))
                }
        }
    }

    companion object {
        fun factory(app: PinkdiaryApp): ViewModelProvider.Factory = viewModelFactory {
            initializer { AppViewModel(app.userSettingsRepository) }
        }
    }
}
