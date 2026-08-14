package com.stephen.pinkdiary.ui.calendar

import androidx.compose.foundation.background
import androidx.compose.foundation.border
import androidx.compose.foundation.clickable
import androidx.compose.foundation.layout.Box
import androidx.compose.foundation.layout.aspectRatio
import androidx.compose.foundation.layout.fillMaxSize
import androidx.compose.foundation.layout.offset
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
    val colors = calendarColors()
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
        isPeriodDay -> colors.period
        isSoftPeriodDay -> colors.softPeriod
        else -> Color.Transparent
    }

    val numberColor = when {
        isPeriodDay -> colors.onPeriod
        isSoftPeriodDay -> colors.onSoftPeriod
        isPredictedDay -> colors.onPredicted
        isCurrentMonth -> MaterialTheme.colorScheme.onSurface
        else -> MaterialTheme.colorScheme.onSurface.copy(alpha = 0.35f)
    }

    Box(
        modifier = modifier
            .aspectRatio(1f),
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
                            color = colors.predicted,
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
        // 仅裁剪点击反馈层，避免影响跨日期连续绘制的经期背景和预测描边。
        Box(
            Modifier
                .fillMaxSize()
                .clip(RoundedCornerShape(MarkCornerRadius))
                .clickable(onClick = onClick)
        )
        if (isSelected) {
            Box(
                Modifier
                    .fillMaxSize()
                    .border(2.dp, colors.selection, RoundedCornerShape(MarkCornerRadius))
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
                    .offset(y = (-6).dp)
                    .size(4.dp)
                    .clip(CircleShape)
                    .background(
                        when {
                            isPeriodDay -> colors.onPeriod
                            else -> colors.selection
                        }
                    )
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
    // 描边以路径为中心向两侧扩展，因此外轮廓需内缩半个线宽，
    // 否则位于日历左右边缘时会有一半落到容器外并被裁切。
    val inset = strokeWidth / 2f
    val left = inset
    val right = w - inset
    val top = inset
    val bottom = h - inset

    val topStartX = if (roundLeft) left + r else 0f
    val topEndX = if (roundRight) right - r else w

    // 顶边、底边
    drawLine(color, Offset(topStartX, top), Offset(topEndX, top), strokeWidth, cap)
    drawLine(color, Offset(topStartX, bottom), Offset(topEndX, bottom), strokeWidth, cap)

    if (roundLeft) {
        drawLine(color, Offset(left, top + r), Offset(left, bottom - r), strokeWidth, cap)
        drawArc(color, 180f, 90f, false, Offset(left, top), Size(2f * r, 2f * r), style = Stroke(width = strokeWidth, cap = cap))
        drawArc(color, 90f, 90f, false, Offset(left, bottom - 2f * r), Size(2f * r, 2f * r), style = Stroke(width = strokeWidth, cap = cap))
    }
    if (roundRight) {
        drawLine(color, Offset(right, top + r), Offset(right, bottom - r), strokeWidth, cap)
        drawArc(color, 270f, 90f, false, Offset(right - 2f * r, top), Size(2f * r, 2f * r), style = Stroke(width = strokeWidth, cap = cap))
        drawArc(color, 0f, 90f, false, Offset(right - 2f * r, bottom - 2f * r), Size(2f * r, 2f * r), style = Stroke(width = strokeWidth, cap = cap))
    }
}
