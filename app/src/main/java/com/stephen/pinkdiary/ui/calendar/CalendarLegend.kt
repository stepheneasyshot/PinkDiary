package com.stephen.pinkdiary.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.layout.Arrangement
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.Row
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.unit.dp
import com.stephen.pinkdiary.ui.theme.PeriodPink
import com.stephen.pinkdiary.ui.theme.PeriodPinkLight

@Composable
fun CalendarLegend(modifier: Modifier = Modifier) {
    Row(
        modifier = modifier,
        horizontalArrangement = Arrangement.spacedBy(16.dp),
        verticalAlignment = Alignment.CenterVertically
    ) {
        LegendItem(fill = PeriodPink, label = "经期")
        LegendItem(border = PeriodPinkLight, label = "预测经期")
        LegendItem(border = MaterialTheme.colorScheme.primary, label = "今天")
    }
}

@Composable
private fun LegendItem(
    fill: Color? = null,
    border: Color? = null,
    label: String
) {
    Row(
        verticalAlignment = Alignment.CenterVertically,
        horizontalArrangement = Arrangement.spacedBy(6.dp)
    ) {
        Box(
            modifier = Modifier
                .size(14.dp)
                .clip(CircleShape)
                .background(fill ?: Color.Transparent)
                .then(if (border != null) Modifier.border(1.5.dp, border, CircleShape) else Modifier)
        )
        Text(
            text = label,
            style = MaterialTheme.typography.labelMedium,
            color = MaterialTheme.colorScheme.onSurfaceVariant
        )
    }
}
