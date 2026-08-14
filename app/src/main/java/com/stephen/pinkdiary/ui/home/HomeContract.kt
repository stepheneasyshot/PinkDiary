package com.stephen.pinkdiary.ui.home

import androidx.annotation.StringRes
import com.stephen.pinkdiary.data.local.PeriodRecord
import com.stephen.pinkdiary.data.prediction.CyclePrediction
import java.time.LocalDate

data class HomeUiState(
    val isLoading: Boolean = true,
    val today: LocalDate = LocalDate.now(),
    val records: List<PeriodRecord> = emptyList(),
    val solidPeriodDates: Set<LocalDate> = emptySet(),
    val softPeriodDates: Set<LocalDate> = emptySet(),
    val predictedDates: Set<LocalDate> = emptySet(),
    val prediction: CyclePrediction? = null,
    val selectedDate: LocalDate? = null,
    val selectedRecord: PeriodRecord? = null,
    val hasOngoingRecord: Boolean = false,
    val isRecordSheetVisible: Boolean = false
)

sealed interface HomeIntent {
    data class DateSelected(val date: LocalDate) : HomeIntent
    data object RecordSheetDismissed : HomeIntent
    data object MarkPeriodStartClicked : HomeIntent
    data object MarkPeriodEndClicked : HomeIntent
    data object DeleteRecordClicked : HomeIntent
}

sealed interface HomeEffect {
    data class ShowMessage(@param:StringRes val messageRes: Int) : HomeEffect
}
