package com.stephen.pinkdiary.ui.calendar

import androidx.compose.foundation.isSystemInDarkTheme
import androidx.compose.runtime.Composable
import androidx.compose.ui.graphics.Color
import java.time.YearMonth

/** 浅色模式：低饱和马卡龙色系 */
private val MacaronLightPalette = listOf(
    Color(0xFFF8D7E3), // 粉
    Color(0xFFD5EFE5), // 薄荷
    Color(0xFFE3DCF2), // 薰衣草
    Color(0xFFFBE3D0), // 蜜桃
    Color(0xFFD3E6F7)  // 天蓝
)

/** 深色模式：对应的低饱和暗色 */
private val MacaronDarkPalette = listOf(
    Color(0xFF3A2A30),
    Color(0xFF22332E),
    Color(0xFF2E2A3A),
    Color(0xFF3A3027),
    Color(0xFF25323A)
)

/**
 * 根据月份返回马卡龙背景色。
 * 相邻月份索引连续，取模后颜色不同，翻页时视觉上更容易区分相邻页。
 */
@Composable
fun macaronColorFor(month: YearMonth): Color {
    val palette = if (isSystemInDarkTheme()) MacaronDarkPalette else MacaronLightPalette
    val index = (month.year * 12 + (month.monthValue - 1)) % palette.size
    return palette[index]
}
