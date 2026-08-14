package com.stephen.pinkdiary.ui.home

import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.padding
import androidx.compose.foundation.rememberScrollState
import androidx.compose.foundation.verticalScroll
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Scaffold
import androidx.compose.material3.Surface
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.runtime.getValue
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.text.style.TextAlign
import androidx.compose.ui.unit.dp
import androidx.lifecycle.compose.collectAsStateWithLifecycle
import com.stephen.pinkdiary.ui.calendar.CalendarLegend
import com.stephen.pinkdiary.ui.calendar.CalendarMonth
import com.stephen.pinkdiary.ui.components.StatusCard
import java.time.YearMonth

@Composable
fun HomeScreen(viewModel: HomeViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()

    Scaffold { innerPadding ->
        Column(
            modifier = Modifier
                .fillMaxSize()
                .padding(innerPadding)
                .verticalScroll(rememberScrollState())
                .padding(horizontal = 16.dp, vertical = 8.dp),
            verticalArrangement = Arrangement.spacedBy(16.dp)
        ) {
            StatusCard(state)
            MonthHeader(
                month = state.displayedMonth,
                onPrevious = viewModel::previousMonth,
                onNext = viewModel::nextMonth
            )
            CalendarMonth(
                month = state.displayedMonth,
                today = state.today,
                periodDates = state.periodDates,
                predictedDates = state.predictedDates,
                selectedDate = state.selectedDate,
                onDateSelected = viewModel::selectDate
            )
            CalendarLegend()
            SelectedDatePanel(state)
        }
    }
}

@Composable
private fun MonthHeader(
    month: YearMonth,
    onPrevious: () -> Unit,
    onNext: () -> Unit
) {
    Row(Modifier.fillMaxWidth(), verticalAlignment = Alignment.CenterVertically) {
        TextButton(onClick = onPrevious) {
            Text("‹", style = MaterialTheme.typography.headlineSmall)
        }
        Text(
            text = "${month.year}年${month.monthValue}月",
            modifier = Modifier.weight(1f),
            textAlign = TextAlign.Center,
            style = MaterialTheme.typography.titleMedium
        )
        TextButton(onClick = onNext) {
            Text("›", style = MaterialTheme.typography.headlineSmall)
        }
    }
}

@Composable
private fun SelectedDatePanel(state: HomeUiState) {
    val date = state.selectedDate ?: state.today
    val label = when {
        date in state.periodDates -> "经期日"
        date in state.predictedDates -> "预测经期日"
        else -> "无标记"
    }

    Surface(
        modifier = Modifier.fillMaxWidth(),
        shape = MaterialTheme.shapes.medium,
        color = MaterialTheme.colorScheme.surfaceVariant
    ) {
        Row(
            modifier = Modifier.padding(16.dp),
            verticalAlignment = Alignment.CenterVertically
        ) {
            Text(
                text = "${date.year}年${date.monthValue}月${date.dayOfMonth}日",
                style = MaterialTheme.typography.bodyLarge
            )
            Spacer(Modifier.weight(1f))
            Text(
                text = label,
                style = MaterialTheme.typography.labelLarge,
                color = MaterialTheme.colorScheme.primary
            )
        }
    }
}
