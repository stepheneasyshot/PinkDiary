package com.stephen.pinkdiary.ui.record

import androidx.compose.foundation.layout.Column
import androidx.compose.foundation.layout.Spacer
import androidx.compose.foundation.layout.fillMaxWidth
import androidx.compose.foundation.layout.height
import androidx.compose.foundation.layout.navigationBarsPadding
import androidx.compose.foundation.layout.padding
import androidx.compose.material3.Button
import androidx.compose.material3.ExperimentalMaterial3Api
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.ModalBottomSheet
import androidx.compose.material3.Text
import androidx.compose.material3.TextButton
import androidx.compose.runtime.Composable
import androidx.compose.ui.Modifier
import androidx.compose.ui.unit.dp
import com.stephen.pinkdiary.data.local.PeriodRecord
import java.time.LocalDate

@OptIn(ExperimentalMaterial3Api::class)
@Composable
fun RecordSheet(
    date: LocalDate,
    coveringRecord: PeriodRecord?,
    isFutureDate: Boolean,
    onDismiss: () -> Unit,
    onMarkStart: () -> Unit,
    onMarkEnd: () -> Unit,
    onDelete: () -> Unit
) {
    ModalBottomSheet(onDismissRequest = onDismiss) {
        Column(
            modifier = Modifier
                .fillMaxWidth()
                .padding(horizontal = 24.dp)
                .padding(bottom = 24.dp)
                .navigationBarsPadding()
        ) {
            Text(
                text = "${date.year}年${date.monthValue}月${date.dayOfMonth}日",
                style = MaterialTheme.typography.titleMedium
            )
            Spacer(Modifier.height(12.dp))

            val isOngoing = coveringRecord != null && coveringRecord.endDateEpochDay == null
            when {
                isOngoing -> {
                    Text("经期进行中", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onMarkEnd, modifier = Modifier.fillMaxWidth()) {
                        Text("标记经期结束")
                    }
                    Spacer(Modifier.height(8.dp))
                    TextButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) {
                        Text("删除这条记录")
                    }
                }
                coveringRecord != null -> {
                    Text("已记录经期", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    TextButton(onClick = onDelete, modifier = Modifier.fillMaxWidth()) {
                        Text("删除这条记录")
                    }
                }
                isFutureDate -> {
                    Text("未来日期无法记录", style = MaterialTheme.typography.bodyMedium)
                }
                else -> {
                    Text("该日暂无经期记录", style = MaterialTheme.typography.bodyMedium)
                    Spacer(Modifier.height(16.dp))
                    Button(onClick = onMarkStart, modifier = Modifier.fillMaxWidth()) {
                        Text("标记经期开始")
                    }
                }
            }
        }
    }
}
