package com.stephen.pinkdiary.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.SnackbarHost
import androidx.compose.material3.SnackbarHostState
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.runtime.LaunchedEffect
import androidx.compose.runtime.getValue
import androidx.compose.runtime.remember
import androidx.compose.runtime.rememberCoroutineScope
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stephen.pinkdiary.R
import com.stephen.pinkdiary.data.prediction.PeriodLogic
import com.stephen.pinkdiary.ui.calendar.CalendarLegend
import com.stephen.pinkdiary.ui.calendar.CalendarPager
import com.stephen.pinkdiary.ui.calendar.WeekdayHeader
import com.stephen.pinkdiary.ui.components.StatusCard
import com.stephen.pinkdiary.ui.record.RecordSheet
import kotlinx.coroutines.launch
import java.time.YearMonth

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val showSheet by viewModel.showSheet.collectAsStateWithLifecycle()
    val userMessage by viewModel.userMessage.collectAsStateWithLifecycle()

    val snackbarHostState = remember { SnackbarHostState() }
    LaunchedEffect(userMessage) {
        userMessage?.let {
            snackbarHostState.showSnackbar(it)
            viewModel.consumeMessage()
        }
    }

    val selectedDate = state.selectedDate ?: state.today
    val coveringRecord = PeriodLogic.coveringRecord(state.records, selectedDate, state.today)
    val ongoingRecord = PeriodLogic.ongoingRecord(state.records)

    // 月份翻页：初始定位到本月，支持左右手势滑动
    val initialMonth = remember { YearMonth.now() }
    val basePage = 1_000_000
    val pagerState = rememberPagerState(initialPage = basePage) { Int.MAX_VALUE }
    val scope = rememberCoroutineScope()
    val currentMonth = initialMonth.plusMonths((pagerState.currentPage - basePage).toLong())

    Box(Modifier.fillMaxSize()) {
        Column(
            modifier = Modifier
                .fillMaxSize()
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(8.dp)
        ) {
            StatusCard(state)
            MonthTitle(currentMonth)
            WeekdayHeader()
            CalendarPager(
                initialMonth = initialMonth,
                pagerState = pagerState,
                basePage = basePage,
                today = state.today,
                solidPeriodDates = state.solidPeriodDates,
                softPeriodDates = state.softPeriodDates,
                predictedDates = state.predictedDates,
                selectedDate = state.selectedDate,
                onDateSelected = viewModel::onDateSelected
            )
            CalendarLegend(
                onJumpToToday = {
                    scope.launch { pagerState.animateScrollToPage(basePage) }
                }
            )
            MarkingGuide(
                isColdStart = state.prediction == null,
                hasOngoing = ongoingRecord != null
            )
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
        )
    }

    if (showSheet) {
        RecordSheet(
            date = selectedDate,
            coveringRecord = coveringRecord,
            isFutureDate = selectedDate.isAfter(state.today),
            onDismiss = viewModel::dismissSheet,
            onMarkStart = { viewModel.markPeriodStart(selectedDate) },
            onMarkEnd = { coveringRecord?.let { viewModel.markPeriodEnd(it.id, selectedDate) } },
            onDelete = { coveringRecord?.let { viewModel.deleteRecord(it.id) } }
        )
    }
}

@Composable
private fun MarkingGuide(isColdStart: Boolean, hasOngoing: Boolean) {
    val text = when {
        isColdStart -> stringResource(R.string.guide_mark_period_start)
        hasOngoing -> stringResource(R.string.guide_mark_period_end)
        else -> stringResource(R.string.guide_mark_default)
    }
    Text(
        text = text,
        style = MaterialTheme.typography.bodyMedium,
        color = MaterialTheme.colorScheme.onSurfaceVariant
    )
}

@Composable
private fun MonthTitle(month: YearMonth) {
    Text(
        text = stringResource(R.string.month_title, month.year, month.monthValue),
        style = MaterialTheme.typography.headlineMedium,
        fontWeight = FontWeight.Bold
    )
}
