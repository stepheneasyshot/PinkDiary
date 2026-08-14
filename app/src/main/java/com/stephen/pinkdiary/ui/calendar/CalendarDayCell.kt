package com.stephen.pinkdiary.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.size
import androidx.compose.foundation.shape.CircleShape
import androidx.compose.foundation.shape.RoundedCornerShape
import androidx.compose.material3.MaterialTheme
import androidx.compose.material3.Text
import androidx.compose.runtime.Composable
import androidx.compose.ui.Alignment
import androidx.compose.ui.Modifier
import androidx.compose.ui.draw.clip
import androidx.compose.ui.draw.drawBehind
import androidx.compose.ui.geometry.Offset
import androidx.compose.ui.geometry.Size
import androidx.compose.ui.graphics.Color
import androidx.compose.ui.graphics.StrokeCap
import androidx.compose.ui.graphics.drawscope.DrawScope
import androidx.compose.ui.graphics.drawscope.Stroke
import androidx.compose.ui.text.font.FontWeight
import androidx.compose.ui.unit.dp
import com.stephen.pinkdiary.ui.theme.PeriodPink
import com.stephen.pinkdiary.ui.theme.PeriodPinkLight
import com.stephen.pinkdiary.ui.theme.PeriodPinkSoft
import com.stephen.pinkdiary.ui.theme.SelectionPink
import java.time.DayOfWeek
import java.time.LocalDate

private val MarkCornerRadius = 14.dp

@Composable
fun CalendarDayCell(
    date: LocalDate,
    isCurrentMonth: Boolean,
    isToday: Boolean,
    solidPeriodDates: Set<LocalDate>,
    softPeriodDates: Set<LocalDate>,
    predictedDates: Set<LocalDate>,
    isSelected: Boolean,
    onClick: () -> Unit,
    modifier: Modifier = Modifier
) {
    val isPeriodDay = date in solidPeriodDates
    val isSoftPeriodDay = date in softPeriodDates
    val isPredictedDay = date in predictedDates

    val prev = date.minusDays(1)
    val next = date.plusDays(1)

    // 与左右相邻格是否「连通」：同类型连续；进行中经期的实心开始日 ↔ 温和后续日
    val connectedLeft = when {
        isPeriodDay -> prev in solidPeriodDates
        isSoftPeriodDay -> prev in softPeriodDates || prev in solidPeriodDates
        isPredictedDay -> prev in predictedDates
        else -> false
    }
    val connectedRight = when {
        isPeriodDay -> next in solidPeriodDates || next in softPeriodDates
        isSoftPeriodDay -> next in softPeriodDates
        isPredictedDay -> next in predictedDates
        else -> false
    }

    val isMarked = isPeriodDay || isSoftPeriodDay || isPredictedDay
    val roundLeft = isMarked && (!connectedLeft || date.dayOfWeek == DayOfWeek.MONDAY)
    val roundRight = isMarked && (!connectedRight || date.dayOfWeek == DayOfWeek.SUNDAY)

    val markShape = when {
        roundLeft && roundRight -> RoundedCornerShape(MarkCornerRadius)
        roundLeft -> RoundedCornerShape(MarkCornerRadius, 0.dp, 0.dp, MarkCornerRadius)
        roundRight -> RoundedCornerShape(0.dp, MarkCornerRadius, MarkCornerRadius, 0.dp)
        else -> RoundedCornerShape(0.dp)
    }

    val background = when {
        isPeriodDay -> PeriodPink
        isSoftPeriodDay -> PeriodPinkSoft
        else -> Color.Transparent
    }

    val numberColor = when {
        isPeriodDay -> Color.White
        isSoftPeriodDay -> PeriodPink
        isPredictedDay -> PeriodPink
        isCurrentMonth -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f)
            .clickable(onClick = onClick),
        contentAlignment = Alignment.Center
    ) {
        if (isPredictedDay) {
            Box(
                Modifier
                    .fillMaxSize()
                    .drawBehind {
                        drawPredictedOutline(
                            roundLeft = roundLeft,
                            roundRight = roundRight,
                            color = PeriodPinkLight,
                            strokeWidth = 1.5.dp.toPx()
                        )
                    }
            )
        } else if (isPeriodDay || isSoftPeriodDay) {
            Box(
                Modifier
                    .fillMaxSize()
                    .clip(markShape)
                    .background(background)
            )
        }
        if (isSelected) {
            Box(
                Modifier
                    .fillMaxSize()
                    .border(2.dp, SelectionPink, RoundedCornerShape(MarkCornerRadius))
            )
        }
        Text(
            text = date.dayOfMonth.toString(),
            style = MaterialTheme.typography.titleMedium,
            fontWeight = if (isToday) FontWeight.Bold else FontWeight.Normal,
            color = numberColor
        )
        if (isToday) {
            Box(
                Modifier
                    .align(Alignment.BottomCenter)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(if (isPeriodDay) Color.White else MaterialTheme.colorScheme.onSurface)
            )
        }
    }
}

/**
 * 绘制预测经期的连通描边：仅外轮廓（顶/底边 + 首尾侧边与圆角），
 * 中间相邻天不绘制左右竖线，避免出现割裂的分割线。
 */
private fun DrawScope.drawPredictedOutline(
    roundLeft: Boolean,
    roundRight: Boolean,
    color: Color,
    strokeWidth: Float
) {
    val w = size.width
    val h = size.height
    val r = MarkCornerRadius.toPx()
    val cap = StrokeCap.Butt

    val topStartX = if (roundLeft) r else 0f
    val topEndX = if (roundRight) w - r else w

    // 顶边、底边
    drawLine(color, Offset(topStartX, 0f), Offset(topEndX, 0f), strokeWidth, cap)
    drawLine(color, Offset(topStartX, h), Offset(topEndX, h), strokeWidth, cap)

    if (roundLeft) {
        drawLine(color, Offset(0f, r), Offset(0f, h - r), strokeWidth, cap)
        drawArc(color, 180f, 90f, false, Offset(0f, 0f), Size(2f * r, 2f * r), style = Stroke(width = strokeWidth, cap = cap))
        drawArc(color, 90f, 90f, false, Offset(0f, h - 2f * r), Size(2f * r, 2f * r), style = Stroke(width = strokeWidth, cap = cap))
    }
    if (roundRight) {
        drawLine(color, Offset(w, r), Offset(w, h - r), strokeWidth, cap)
        drawArc(color, 270f, 90f, false, Offset(w - 2f * r, 0f), Size(2f * r, 2f * r), style = Stroke(width = strokeWidth, cap = cap))
        drawArc(color, 0f, 90f, false, Offset(w - 2f * r, h - 2f * r), Size(2f * r, 2f * r), style = Stroke(width = strokeWidth, cap = cap))
    }
}
