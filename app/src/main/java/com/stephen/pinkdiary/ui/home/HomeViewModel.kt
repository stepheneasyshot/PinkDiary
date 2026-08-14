package com.stephen.pinkdiary.ui.home

import android.app.Application
import androidx.lifecycle.ViewModel
import androidx.lifecycle.ViewModelProvider
import androidx.lifecycle.viewModelScope
import androidx.lifecycle.viewmodel.initializer
import androidx.lifecycle.viewmodel.viewModelFactory
import com.stephen.pinkdiary.PinkdiaryApp
import com.stephen.pinkdiary.R
import com.stephen.pinkdiary.data.local.PeriodRecord
import com.stephen.pinkdiary.data.prediction.CalendarMarks
import com.stephen.pinkdiary.data.prediction.CyclePrediction
import com.stephen.pinkdiary.data.prediction.CyclePredictor
import com.stephen.pinkdiary.data.repository.PeriodEndBeforeStartException
import com.stephen.pinkdiary.data.repository.PeriodRepository
import com.stephen.pinkdiary.data.repository.UserSettingsRepository
import kotlinx.coroutines.flow.MutableStateFlow
import kotlinx.coroutines.flow.SharingStarted
import kotlinx.coroutines.flow.StateFlow
import kotlinx.coroutines.flow.combine
import kotlinx.coroutines.flow.stateIn
import kotlinx.coroutines.launch
import java.time.LocalDate

data class HomeUiState(
    val today: LocalDate = LocalDate.now(),
    val records: List<PeriodRecord> = emptyList(),
    val solidPeriodDates: Set<LocalDate> = emptySet(),
    val softPeriodDates: Set<LocalDate> = emptySet(),
    val predictedDates: Set<LocalDate> = emptySet(),
    val prediction: CyclePrediction? = null,
    val selectedDate: LocalDate? = null
)

class HomeViewModel(
    private val app: Application,
    private val periodRepository: PeriodRepository,
    private val settingsRepository: UserSettingsRepository
) : ViewModel() {

    private val selectedDate = MutableStateFlow<LocalDate?>(null)

    private val _showSheet = MutableStateFlow(false)
    val showSheet: StateFlow<Boolean> = _showSheet

    private val _userMessage = MutableStateFlow<String?>(null)
    val userMessage: StateFlow<String?> = _userMessage

    val uiState: StateFlow<HomeUiState> = combine(
        periodRepository.records,
        settingsRepository.settings,
        selectedDate
    ) { records, settings, selected ->
        val today = LocalDate.now()
        val prediction = CyclePredictor.predict(records, settings, today)
        HomeUiState(
            today = today,
            records = records,
            solidPeriodDates = CalendarMarks.solidPeriodDates(records),
            softPeriodDates = CalendarMarks.softPeriodDates(records, today),
            predictedDates = CalendarMarks.predictedPeriodDates(prediction),
            prediction = prediction,
            selectedDate = selected
        )
    }.stateIn(viewModelScope, SharingStarted.WhileSubscribed(5_000), HomeUiState())

    /** 点击日期：选中并弹出记录面板 */
    fun onDateSelected(date: LocalDate) {
        selectedDate.value = date
        _showSheet.value = true
    }

    fun dismissSheet() {
        _showSheet.value = false
    }

    fun consumeMessage() {
        _userMessage.value = null
    }

    fun markPeriodStart(date: LocalDate) {
        viewModelScope.launch {
            periodRepository.markPeriodStart(date.toEpochDay())
            _showSheet.value = false
        }
    }

    fun markPeriodEnd(recordId: Long, endDate: LocalDate) {
        viewModelScope.launch {
            runCatching { periodRepository.markPeriodEnd(recordId, endDate.toEpochDay()) }
                .onSuccess { _showSheet.value = false }
                .onFailure { e ->
                    _userMessage.value = when (e) {
                        is PeriodEndBeforeStartException -> app.getString(R.string.error_end_before_start)
                        else -> app.getString(R.string.error_generic)
                    }
                }
        }
    }

    fun deleteRecord(recordId: Long) {
        viewModelScope.launch {
            periodRepository.deleteById(recordId)
            _showSheet.value = false
        }
    }

    companion object {
        fun factory(app: PinkdiaryApp): ViewModelProvider.Factory = viewModelFactory {
            initializer {
                HomeViewModel(app, app.periodRepository, app.userSettingsRepository)
            }
        }
    }
}
