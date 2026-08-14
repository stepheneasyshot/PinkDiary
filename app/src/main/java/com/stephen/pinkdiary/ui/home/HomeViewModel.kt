package com.stephen.pinkdiary.ui.home

import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.stephen.pinkdiary.PinkdiaryApp
import com.stephen.pinkdiary.data.prediction.CalendarMarks
import com.stephen.pinkdiary.data.prediction.CyclePrediction
import com.stephen.pinkdiary.data.prediction.CyclePredictor
import com.stephen.pinkdiary.data.repository.PeriodRepository
import com.stephen.pinkdiary.data.repository.UserSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.flow.update
import java.time.LocalDate
import java.time.YearMonth

data class HomeUiState(
    val displayedMonth: YearMonth = YearMonth.now(),
    val today: LocalDate = LocalDate.now(),
    val periodDates: Set<LocalDate> = emptySet(),
    val predictedDates: Set<LocalDate> = emptySet(),
    val prediction: CyclePrediction? = null,
    val selectedDate: LocalDate? = null
)

class HomeViewModel(
    private val periodRepository: PeriodRepository,
    private val settingsRepository: UserSettingsRepository
) : ViewModel() {

    private val displayedMonth = MutableStateFlow(YearMonth.now())
    private val selectedDate = MutableStateFlow<LocalDate?>(null)

    val uiState: StateFlow<HomeUiState> = combine(
        periodRepository.records,
        settingsRepository.settings,
        displayedMonth,
        selectedDate
    ) { records, settings, month, selected ->
        val today = LocalDate.now()
        val prediction = CyclePredictor.predict(records, settings, today)
        HomeUiState(
            displayedMonth = month,
            today = today,
            periodDates = CalendarMarks.recordedPeriodDates(records, today),
            predictedDates = CalendarMarks.predictedPeriodDates(prediction),
            prediction = prediction,
            selectedDate = selected
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    fun previousMonth() = displayedMonth.update { it.minusMonths(1) }

    fun nextMonth() = displayedMonth.update { it.plusMonths(1) }

    fun selectDate(date: LocalDate) = selectedDate.update { date }

    companion object {
        fun factory(app: PinkdiaryApp): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HomeViewModel(app.periodRepository, app.userSettingsRepository)
            }
        }
    }
}
