package com.stephen.pinkdiary.ui.home

import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.stephen.pinkdiary.PinkdiaryApp
import com.stephen.pinkdiary.R
import com.stephen.pinkdiary.data.prediction.CalendarMarks
import com.stephen.pinkdiary.data.prediction.CyclePredictor
import com.stephen.pinkdiary.data.prediction.PeriodLogic
import com.stephen.pinkdiary.data.repository.PeriodEndBeforeStartException
import com.stephen.pinkdiary.data.repository.PeriodRepository
import com.stephen.pinkdiary.data.repository.UserSettingsRepository
import com.stephen.pinkdiary.ui.mvi.MviViewModel
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.launch
import java.time.LocalDate

class HomeViewModel(
    private val periodRepository: PeriodRepository,
    settingsRepository: UserSettingsRepository,
    private val todayProvider: () -> LocalDate = LocalDate::now
) : MviViewModel<HomeIntent, HomeUiState, HomeEffect>(
    HomeUiState(today = todayProvider())
) {

    init {
        viewModelScope.launch {
            combine(periodRepository.records, settingsRepository.settings) { records, settings ->
                records to settings
            }.collect { (records, settings) ->
                val today = todayProvider()
                val prediction = CyclePredictor.predict(records, settings, today)
                reduce { current ->
                    current.copy(
                        isLoading = false,
                        today = today,
                        records = records,
                        solidPeriodDates = CalendarMarks.solidPeriodDates(records),
                        softPeriodDates = CalendarMarks.softPeriodDates(records, today),
                        predictedDates = CalendarMarks.predictedPeriodDates(prediction),
                        prediction = prediction,
                        selectedRecord = current.selectedDate?.let { selected ->
                            PeriodLogic.coveringRecord(records, selected, today)
                        },
                        hasOngoingRecord = PeriodLogic.ongoingRecord(records) != null
                    )
                }
            }
        }
    }

    override fun onIntent(intent: HomeIntent) {
        when (intent) {
            is HomeIntent.DateSelected -> selectDate(intent.date)
            HomeIntent.MarkPeriodStartClicked -> markPeriodStart()
            HomeIntent.MarkPeriodEndClicked -> markPeriodEnd()
            HomeIntent.DeleteRecordClicked -> deleteRecord()
        }
    }

    private fun selectDate(date: LocalDate) {
        reduce { current ->
            current.copy(
                selectedDate = date,
                selectedRecord = PeriodLogic.coveringRecord(current.records, date, current.today)
            )
        }
    }

    private fun markPeriodStart() {
        val date = uiState.value.selectedDate ?: return
        performRecordAction { periodRepository.markPeriodStart(date.toEpochDay()) }
    }

    private fun markPeriodEnd() {
        val state = uiState.value
        val date = state.selectedDate ?: return
        val record = state.selectedRecord ?: return
        performRecordAction { periodRepository.markPeriodEnd(record.id, date.toEpochDay()) }
    }

    private fun deleteRecord() {
        val record = uiState.value.selectedRecord ?: return
        performRecordAction { periodRepository.deleteById(record.id) }
    }

    private fun performRecordAction(action: suspend () -> Unit) {
        viewModelScope.launch {
            runCatching { action() }
                .onFailure { error ->
                    val messageRes = when (error) {
                        is PeriodEndBeforeStartException -> R.string.error_end_before_start
                        else -> R.string.error_generic
                    }
                    emitEffect(HomeEffect.ShowMessage(messageRes))
                }
        }
    }

    companion object {
        fun factory(app: PinkdiaryApp): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HomeViewModel(app.periodRepository, app.userSettingsRepository)
            }
        }
    }
}
