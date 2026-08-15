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

## 3. 记录规则（`PeriodRepository` / `RoomPeriodRepository`）

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
| `solidPeriodDates(records)` | 已结束记录 `start..end`（含首尾）+ 进行中记录的 `start` 日 | **高对比实心小圆**（确认的经期 + 进行中的开始日）|
| `softPeriodDates(records, today)` | 进行中记录 `start+1 .. today` | **温和**标记（进行中、尚未结束）|
| `predictedPeriodDates(prediction)` | `predictedStart .. predictedEnd`（含首尾）| **预测**标记（淡藕粉纯色小圆）|
| `cyclePhaseDates(records, prediction, today)` | 卵泡期 / 预测排卵日 / 黄体期集合 | **估算**周期阶段（低对比辅助标记） |

### 5.1 周期阶段估算

- 每个周期以一次经期开始日为起点，以下一次经期开始日为边界；历史周期使用真实记录，当前周期使用 `predictedStart`。与预测规则一致，仅对 15–60 天的有效周期生成阶段标记，过长间隔不做医学含义推断。
- 预测排卵日：`下次经期开始日 - 14 天`。这是日历估算值，不是排卵检测结果。
- 卵泡期标记：为避免覆盖更重要的经期标记，UI 只标记「出血结束次日 .. 预测排卵日前一日」。生理学上卵泡期从周期第 1 天开始，与月经期存在重叠；这里只是视觉分层。
- 黄体期标记：`预测排卵日 + 1 .. 下次经期开始日 - 1`。
- 未标记结束的进行中经期，按 `max(今天, 开始日 + 平均经期长度 - 1)` 作为视觉上的经期结束边界，避免阶段标记覆盖已确认的进行中日期。
- 排卵时间会在个体间及同一个体的不同周期波动；日历阶段不得作为避孕、诊断或确认排卵的依据。

**科学依据（复核：2026-08-15）**：

- [ACOG：排卵通常约在下次经期前 14 天，无法仅靠日历精确确定](https://www.acog.org/womens-health/experts-and-stories/the-latest/trying-to-get-pregnant-heres-when-to-have-sex)
- [NHS：多数人排卵约在下次经期前 10–16 天](https://www.nhs.uk/conditions/periods/fertility-in-the-menstrual-cycle/)
- [NICHD：周期、卵泡发育、排卵与子宫内膜变化](https://www.nichd.nih.gov/health/topics/menstruation/conditioninfo)
- [CDC：即使周期规律，易孕窗时间仍可显著波动](https://www.cdc.gov/contraception/hcp/usspr/standard-days-method.html)

## 6. 记录查询（`PeriodLogic`，纯函数）

- `ongoingRecord(records)`：返回第一条 `endDateEpochDay == null` 的记录。
- `coveringRecord(records, date, today)`：返回覆盖某日的记录，判定条件 `start <= date <= (end ?: today)`；未来日期不会被进行中记录覆盖。

## 7. 视觉呈现规则（`ui/calendar/`）

- 所有日期状态都采用**独立的 28.dp 小圆形**标记，不再将连续日期连成色带。
- 已记录经期使用高对比莓红实心圆；进行中的后续日使用浅藕粉实心圆。
- 预测经期使用淡藕粉纯色小圆，不加描边，在保持独立圆形的同时与已记录经期区分。
- 颜色采用同一套莓红/藕粉色阶，并提供深色模式变体：
  - 实心经期：莓红实心 + 白字，突出已确认记录。
  - 温和/待选：浅藕粉实心 + 深莓红字，与已确认记录保持关联但降低强调。
  - 预测经期：淡藕粉纯色小圆 + 深色数字，不再使用低对比度浅粉字。
  - 卵泡期 / 预测排卵日 / 黄体期：日期数字后的小面积、低饱和浅色圆形，对比度低于已记录与预测经期。
  - 选中日期：围绕日期标记显示浅玫瑰色同心圆环，日期文字保留其原本状态色，不遮盖经期信息。
- 日历背景统一为中性极浅灰，与淡藕粉预测标记拉开明度差，同时避免大面积彩色底色干扰经期状态识别。

## 8. 状态卡文案（`StatusCard`，纯函数 `buildStatusCardModel`）

| 状态 | 触发条件 | 标题 |
|------|----------|------|
| 冷启动 | `prediction == null` | 开始记录你的经期 |
| 经期中 | `isOnPeriod` | 经期中 · 第 X 天 |
| 预测已过期 | `daysUntilNext < 0` | 预测已过 X 天，请记录 |
| 预测今天 | `daysUntilNext == 0` | 预测今天开始 |
| 正常 | `daysUntilNext > 0` | 距下次经期 X 天 |

---

> 本文档对应实现：`data/prediction/CyclePredictor.kt`、`CalendarMarks.kt`、`PeriodLogic.kt`，`data/repository/PeriodRepository.kt` 中的 `RoomPeriodRepository`，`ui/components/StatusCard.kt`。规则变更时请同步更新本文档与相应单测。
