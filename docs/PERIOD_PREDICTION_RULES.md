# 经期计算与预测规则

本文档沉淀现有代码中「经期记录 → 周期/经期长度计算 → 下次经期预测 → 日历标记」的完整规则，与 `data/prediction/`、`data/repository/` 中的实现一一对应。

## 1. 领域概念

| 术语 | 定义 | 说明 |
|------|------|------|
| 经期记录（PeriodRecord） | 一次经期 = 一条记录（start + end?） | 片段式建模 |
| 周期长度（Cycle Length） | 相邻两次经期开始日之差（天） | 用于预测 |
| 经期长度（Period Length） | `end - start + 1`（天） | 仅对已结束记录 |
| 周期第 N 天（Cycle Day） | `today - 最近开始日 + 1` | 当前周期进度 |
| 进行中（Ongoing） | `endDateEpochDay == null` 的记录 | 尚未标记结束 |

## 2. 数据模型

### 2.1 经期记录（Room）

```kotlin
@Entity(tableName = "period_records")
data class PeriodRecord(
    val id: Long,                    // 自增主键
    val startDateEpochDay: Long,     // 经期开始日（必填）
    val endDateEpochDay: Long?,      // 经期结束日；null = 进行中
    val note: String?,               // 备注（可选）
    val createdAt: Long,
    val updatedAt: Long
)
```

> 约定：日期统一用 `epochDay: Long` 存储，展示时转 `LocalDate`，只精确到「天」。

### 2.2 用户设置（DataStore）

```kotlin
data class UserSettings(
    val defaultCycleLength: Int = 28,  // 默认周期长度（冷启动兜底）
    val defaultPeriodLength: Int = 5,  // 默认经期长度（冷启动兜底）
    val recentN: Int = 6               // 参与平均的最近周期数
)
```

## 3. 记录规则（`PeriodRepository`）

### 3.1 标记经期开始 `markPeriodStart(startEpochDay)`

1. 若同日开始日已存在 → 视为修改，直接返回该记录 id（幂等）。
2. 若存在「进行中」记录，且新开始日晚于其开始日 → **自动闭合**上一次经期：`end = 新开始日 - 1`。
3. 插入新记录：`start = 开始日, end = null`（进行中）。

### 3.2 标记经期结束 `markPeriodEnd(recordId, endEpochDay)`

- 校验 `endEpochDay >= startDateEpochDay`，否则抛 `IllegalArgumentException("结束日不能早于开始日")`。
- 更新记录的 `endDateEpochDay`。

### 3.3 删除

- `deleteById(id)` 删除整条记录；`updateNote(id, note)` 更新备注。

## 4. 预测算法（`CyclePredictor`，纯函数）

### 4.1 输入

- 历史记录 `List<PeriodRecord>`（含进行中）
- 用户设置 `UserSettings`
- 今天 `today: LocalDate`

### 4.2 步骤

```
1. 开始日列表 starts = 所有记录的开始日，升序排序
2. 周期长度列表 cycleLengths[i] = starts[i+1] - starts[i]
3. 过滤异常周期：仅保留 15 ~ 60 天（MIN_VALID_CYCLE=15, MAX_VALID_CYCLE=60）
4. 平均周期 avgCycle：
   - 无有效周期            → defaultCycleLength（默认 28）
   - 否则取「最近 recentN 个」的算术平均（不足 recentN 则用全部），四舍五入
5. 平均经期长度 avgPeriod：
   - 所有「已结束」记录的 (end - start + 1) 取平均，四舍五入
   - 无已结束记录           → defaultPeriodLength（默认 5）
6. 预测：
   predictedStart = 最近开始日 + avgCycle（天）
   predictedEnd   = predictedStart + (avgPeriod - 1)（天）
   durationDays   = avgPeriod
7. 周期状态：
   cycleDay      = today - 最近开始日 + 1
   daysUntilNext = predictedStart - today   （可为负：预测日已过但未记录）
8. 进行中判定：
   ongoing = 存在 endDateEpochDay == null 且 startDateEpochDay <= today 的记录
   periodDay = today - ongoing.startDateEpochDay + 1
```

### 4.3 冷启动

- **无任何历史记录时，`predict()` 返回 `null`**（无法锚定开始日）。
- UI 据此展示「开始记录你的经期」引导，不展示预测区间。

### 4.4 边界情况

- `daysUntilNext < 0`：预测开始日已过但用户未记录 → 提示「预测已过 X 天，请记录」。
- 进行中经期的结束日未定，**不参与**周期长度计算（周期只由开始日差决定），但决定「今天是否在经期中」与「周期第几天」。

## 5. 日历标记规则（`CalendarMarks`，纯函数）

| 函数 | 结果集合 | 含义 |
|------|----------|------|
| `solidPeriodDates(records)` | 已结束记录 `start..end`（含首尾）+ 进行中记录的 `start` 日 | **实心**标记（确认的经期 + 进行中的开始日）|
| `softPeriodDates(records, today)` | 进行中记录 `start+1 .. today` | **温和**标记（进行中、尚未结束）|
| `predictedPeriodDates(prediction)` | `predictedStart .. predictedEnd`（含首尾）| **预测**标记（空心描边）|

## 6. 记录查询（`PeriodLogic`，纯函数）

- `ongoingRecord(records)`：返回第一条 `endDateEpochDay == null` 的记录。
- `coveringRecord(records, date, today)`：返回覆盖某日的记录，判定条件 `start <= date <= (end ?: today)`；未来日期不会被进行中记录覆盖。

## 7. 视觉呈现规则（`ui/calendar/`）

- 标记采用**连通的圆角矩形**（`MarkCornerRadius = 14.dp`），不再是独立圆形。
- 圆角判定：
  - 范围「首/尾」侧圆角；中间直角相连。
  - 额外在**行首（周一）加左圆角、行尾（周日）加右圆角**，避免直角顶到网格边缘。
  - 进行中经期的「实心开始日 ↔ 温和后续日」**跨类型连通**（开始日右侧、后续日左侧不圆角）。
- 预测经期用**仅外轮廓**描边（顶/底边 + 首尾侧边与圆角），中间相邻天不画左右竖线。
- 颜色（低饱和藕粉系）：
  - 实心经期：`PeriodPink = 0xFFB77A8E`
  - 温和/待选：`PeriodPinkSoft = PeriodPink.copy(alpha = 0.22f)`
  - 预测描边：`PeriodPinkLight = 0xFFC98A9E`
  - 选中框线：`SelectionPink = 0xFFC2185B`（深粉圆角矩形）
- 翻页背景：5 色低饱和马卡龙（粉/薄荷/薰衣草/蜜桃/天蓝），按「年×12+月」取模，**相邻月份不同色**；深色模式配暗色变体。

## 8. 状态卡文案（`StatusCard`，纯函数 `buildStatusCardModel`）

| 状态 | 触发条件 | 标题 |
|------|----------|------|
| 冷启动 | `prediction == null` | 开始记录你的经期 |
| 经期中 | `isOnPeriod` | 经期中 · 第 X 天 |
| 预测已过期 | `daysUntilNext < 0` | 预测已过 X 天，请记录 |
| 预测今天 | `daysUntilNext == 0` | 预测今天开始 |
| 正常 | `daysUntilNext > 0` | 距下次经期 X 天 |

---

> 本文档对应实现：`data/prediction/CyclePredictor.kt`、`CalendarMarks.kt`、`PeriodLogic.kt`，`data/repository/PeriodRepository.kt`，`ui/components/StatusCard.kt`。规则变更时请同步更新本文档与相应单测。
