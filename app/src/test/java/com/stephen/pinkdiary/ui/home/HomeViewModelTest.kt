package com.stephen.pinkdiary.ui.home

import com.stephen.pinkdiary.R
import com.stephen.pinkdiary.data.local.PeriodRecord
import com.stephen.pinkdiary.data.prediction.CyclePhase
import com.stephen.pinkdiary.testutil.FakePeriodRepository
import com.stephen.pinkdiary.testutil.FakeUserSettingsRepository
import com.stephen.pinkdiary.testutil.MainDispatcherRule
import kotlinx.coroutines.ExperimentalCoroutinesApi
import kotlinx.coroutines.async
import kotlinx.coroutines.flow.first
import kotlinx.coroutines.test.advanceUntilIdle
import kotlinx.coroutines.test.runTest
import org.junit.Assert.assertEquals
import org.junit.Rule
import org.junit.Test
import java.time.LocalDate

@OptIn(ExperimentalCoroutinesApi::class)
class HomeViewModelTest {

    @get:Rule
    val mainDispatcherRule = MainDispatcherRule()

    private val today = LocalDate.of(2026, 8, 15)

    @Test
    fun `date selection remains visible after inline mark start action`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val periods = FakePeriodRepository()
            val viewModel = HomeViewModel(
                periodRepository = periods,
                settingsRepository = FakeUserSettingsRepository(),
                todayProvider = { today }
            )
            advanceUntilIdle()

            viewModel.onIntent(HomeIntent.DateSelected(today))
            assertEquals(today, viewModel.uiState.value.selectedDate)

            viewModel.onIntent(HomeIntent.MarkPeriodStartClicked)
            advanceUntilIdle()

            assertEquals(today.toEpochDay(), periods.lastMarkedStart)
            assertEquals(today, viewModel.uiState.value.selectedDate)
        }

    @Test
    fun `repository failure emits message effect`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val periods = FakePeriodRepository().apply { failNextWrite = true }
            val viewModel = HomeViewModel(
                periodRepository = periods,
                settingsRepository = FakeUserSettingsRepository(),
                todayProvider = { today }
            )
            advanceUntilIdle()
            viewModel.onIntent(HomeIntent.DateSelected(today))
            val effect = async { viewModel.effects.first() }

            viewModel.onIntent(HomeIntent.MarkPeriodStartClicked)
            advanceUntilIdle()

            assertEquals(HomeEffect.ShowMessage(R.string.error_generic), effect.await())
        }

    @Test
    fun `repository records produce cycle phase dates in ui state`() =
        runTest(mainDispatcherRule.testDispatcher) {
            val start = LocalDate.of(2026, 8, 1)
            val periods = FakePeriodRepository(
                listOf(
                    PeriodRecord(
                        id = 1,
                        startDateEpochDay = start.toEpochDay(),
                        endDateEpochDay = LocalDate.of(2026, 8, 5).toEpochDay(),
                        createdAt = 0,
                        updatedAt = 0
                    )
                )
            )
            val viewModel = HomeViewModel(
                periodRepository = periods,
                settingsRepository = FakeUserSettingsRepository(),
                todayProvider = { today }
            )

            advanceUntilIdle()

            assertEquals(
                CyclePhase.FOLLICULAR,
                viewModel.uiState.value.cyclePhaseDates.phaseOn(LocalDate.of(2026, 8, 6))
            )
            assertEquals(
                CyclePhase.OVULATION,
                viewModel.uiState.value.cyclePhaseDates.phaseOn(LocalDate.of(2026, 8, 15))
            )
            assertEquals(
                CyclePhase.LUTEAL,
                viewModel.uiState.value.cyclePhaseDates.phaseOn(LocalDate.of(2026, 8, 16))
            )
        }
}
