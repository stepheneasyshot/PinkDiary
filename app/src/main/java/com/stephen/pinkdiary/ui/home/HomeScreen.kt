package com.stephen.pinkdiary.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.pager.rememberPagerState
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
import androidx.compose.ui.platform.LocalContext
import androidx.compose.ui.res.stringResource
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stephen.pinkdiary.R
import com.stephen.pinkdiary.ui.calendar.CalendarLegend
import com.stephen.pinkdiary.ui.calendar.CalendarPager
import com.stephen.pinkdiary.ui.calendar.WeekdayHeader
import com.stephen.pinkdiary.ui.components.StatusCard
import com.stephen.pinkdiary.ui.record.RecordActions
import kotlinx.coroutines.launch
import java.time.YearMonth

@Composable
fun HomeRoute(viewModel: HomeViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    val snackbarHostState = remember { SnackbarHostState() }
    val context = LocalContext.current

    LaunchedEffect(viewModel) {
        viewModel.effects.collect { effect ->
            when (effect) {
                is HomeEffect.ShowMessage -> snackbarHostState.showSnackbar(
                    context.getString(effect.messageRes)
                )
            }
        }
    }

    HomeScreen(
        state = state,
        snackbarHostState = snackbarHostState,
        onIntent = viewModel::onIntent
    )
}

@Composable
fun HomeScreen(
    state: HomeUiState,
    snackbarHostState: SnackbarHostState,
    onIntent: (HomeIntent) -> Unit
) {
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
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(6.dp)
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
                onDateSelected = { onIntent(HomeIntent.DateSelected(it)) }
            )
            CalendarLegend(
                onJumpToToday = {
                    scope.launch { pagerState.animateScrollToPage(basePage) }
                }
            )
            state.selectedDate?.let { selectedDate ->
                RecordActions(
                    coveringRecord = state.selectedRecord,
                    isFutureDate = selectedDate.isAfter(state.today),
                    onMarkStart = { onIntent(HomeIntent.MarkPeriodStartClicked) },
                    onMarkEnd = { onIntent(HomeIntent.MarkPeriodEndClicked) },
                    onDelete = { onIntent(HomeIntent.DeleteRecordClicked) }
                )
            }
        }
        SnackbarHost(
            hostState = snackbarHostState,
            modifier = Modifier.align(Alignment.BottomCenter)
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
