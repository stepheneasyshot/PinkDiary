package com.stephen.pinkdiary.ui.settings

import com.stephen.pinkdiary.testutil.FakeUserSettingsRepository
import com.stephen.pinkdiary.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test

@OptIn(ExperimentalCoroutinesApi::class)
class SettingsViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    @Test
    fun `cycle length intent updates state and repository`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val repository = FakeUserSettingsRepository()
            val viewModel = SettingsViewModel(repository)
            advanceUntilIdle()

            viewModel.onIntent(SettingsIntent.DefaultCycleLengthChanged(30))
            assertEquals(30, viewModel.uiState.value.settings.defaultCycleLength)
            advanceUntilIdle()

            assertEquals(30, repository.lastUpdatedSettings?.defaultCycleLength)
        }

    @Test
    fun `out of range intent is clamped at architecture boundary`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val viewModel = SettingsViewModel(FakeUserSettingsRepository())
            advanceUntilIdle()

            viewModel.onIntent(SettingsIntent.RecentCyclesChanged(Int.MAX_VALUE))

            assertEquals(
                SettingsViewModel.MAX_RECENT_CYCLES,
                viewModel.uiState.value.settings.recentN
            )
        }
}
