package com.stephen.pinkdiary.ui.app

import androidx.annotation.StringRes

enum class AppDestination {
    ONBOARDING,
    MAIN
}

data class AppUiState(
    val destination: AppDestination? = null,
    val isCompletingOnboarding: Boolean = false
)

sealed interface AppIntent {
    data object OnboardingFinished : AppIntent
}

sealed interface AppEffect {
    data class ShowMessage(@param:StringRes val messageRes: Int) : AppEffect
}
