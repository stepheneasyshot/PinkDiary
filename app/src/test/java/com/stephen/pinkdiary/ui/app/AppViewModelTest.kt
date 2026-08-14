package com.stephen.pinkdiary.ui.app

import com.stephen.pinkdiary.testutil.FakeUserSettingsRepository
import com.stephen.pinkdiary.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class AppViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `onboarding completion intent moves app to main destination`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = AppViewModel(FakeUserSettingsRepository())
            advanceUntilIdle()
            assertEquals(AppDestination.ONBOARDING, viewModel.uiState.value.destination)

            viewModel.onIntent(AppIntent.OnboardingFinished)
            advanceUntilIdle()

            assertEquals(AppDestination.MAIN, viewModel.uiState.value.destination)
        }
}
