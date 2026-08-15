# Pinkdiary 经期记录与预测 — 功能方案设计

> 版本：v1.0（初期 MVP）
> 技术栈：Kotlin 2.2 · Jetpack Compose · Material 3 · Room · DataStore · 严格 MVI

---

## 1. 产品目标

帮助女性用户**记录**每次月经的开始/结束时间，并基于历史数据**自动预测**下一次月经的开始、结束与持续时间。

初期聚焦三件事：

1. **日历**：以月视图直观展示经期、预测经期、估算周期阶段、今天、选中日期。
2. **记录**：标记经期开始日与结束日，可修改、删除历史记录。
3. **预测**：根据历史周期与经期长度，预测下次经期开始日、结束日、持续时间。

---

## 2. 领域概念与术语

| 术语 | 定义 | 常见范围 |
|------|------|----------|
| 月经周期（Cycle） | 从一次月经第 1 天，到下一次月经第 1 天的天数 | 21–35 天，均值 28 天 |
| 经期（Period） | 一次月经出血持续的天数 | 2–8 天，均值 5 天 |
| 周期长度（Cycle Length） | 相邻两次经期开始日之差 | 21–35 天 |
| 经期长度（Period Length） | 结束日 − 开始日 + 1 | 2–8 天 |
| 周期第 N 天（Cycle Day） | 今天 − 最近一次开始日 + 1 | — |
| 预测排卵日（Estimated Ovulation） | 按下次经期前 14 天估算，实际时间可波动 | 仅供记录参考，不可用于避孕 |

> 约定：**日期只精确到「天」**，用 `java.time.LocalDate`（存 `epochDay: Long`）表示，不涉及时区与时分秒。

---

## 3. 功能范围

### 3.1 初期 MVP（本期交付）

| 模块 | 功能点 | 说明 |
|------|--------|------|
| 日历 | 月视图 | 年份/月份标题 + 左右箭头（可滑动）切换月份 |
| 日历 | 状态标记 | 经期日、预测经期日、估算卵泡期/排卵日/黄体期、今天、选中日期、非本月日期 |
| 日历 | 图例 | 底部图例说明各颜色/图形含义 |
| 记录 | 标记开始 | 选择某天 → 「标记经期开始」 |
| 记录 | 标记结束 | 对进行中的经期 → 「标记经期结束」 |
| 记录 | 修改/删除 | 长按或详情面板编辑、清除历史记录 |
| 预测 | 下次经期 | 预测开始日、结束日、持续时间 |
| 预测 | 周期状态 | 今天处于周期第几天、距下次经期还有几天 |
| 首页 | 状态卡 | 顶部卡片汇总「经期中 / 距下次 X 天 / 预测今日开始」 |
| 设置 | 默认值 | 默认周期长度、默认经期长度、参与平均的历史周期数 |
| 科普 | 文章列表与详情 | 首批内置 15 篇文章；列表展示标题和摘要，点击后从本地 Markdown 加载审核过的正文并原生 Compose 渲染 |

### 3.2 明确不在初期范围（预留扩展点，不实现）

- 易孕期 / 安全期或生育概率推断（当前只做低确定性的阶段估算）
- 症状、流量、情绪、性行为等日志
- 多用户、账号体系、云同步
- 怀孕模式、孕期切换
- 提醒/通知（可在后续加 `AlarmManager` + 通知权限）
- 温度、LH 试纸等硬件/数据导入

---

## 4. 核心预测算法

算法设计为**纯函数**（无 Android 依赖），便于单元测试。

### 4.1 输入

- 历史经期记录列表（每条含 `startDate`、`endDate?`）
- 用户设置：`defaultCycleLength`（默认 28）、`defaultPeriodLength`（默认 5）、`recentN`（默认 6）

### 4.2 步骤（伪代码）

```
1. 按开始日升序排序，得到 starts[]
2. 周期长度列表 = starts 相邻两项之差
   cycleLengths[i] = starts[i+1] - starts[i]
3. 过滤异常周期：长度 < 15 或 > 60 的剔除（避免误录污染平均）
4. 平均周期：
   - 若有效周期数 == 0 → 使用 defaultCycleLength
   - 否则取「最近 recentN 个」的算术平均（不足 recentN 则用全部）
5. 经期长度：
   - 取所有「已结束」记录的 (end - start + 1)
   - 取平均；若无任何已结束记录 → 使用 defaultPeriodLength
6. 预测：
   predictedStart = 最近一次开始日 + 平均周期（天）
   predictedEnd   = predictedStart + 平均经期长度 - 1
   duration       = 平均经期长度
```

### 4.3 周期状态计算

```
cycleDay       = 今天 - 最近一次开始日 + 1          // 周期第几天
daysUntilNext  = predictedStart - 今天              // 距下次经期天数
```

- `daysUntilNext < 0`：已过预测开始日但用户尚未记录 → 提示「可能已开始，请记录」。
- 存在 `endDate == null` 的进行中经期 → 状态为「经期中，第 (今天 - start + 1) 天」。

### 4.4 冷启动策略

- 无任何历史数据时，用设置里的默认值（28 / 5）展示「预测」，并明确标注为**估算**。
- 用户记录第一笔经期开始后，预测开始逐步过渡到基于真实数据的平均值。
- 数据量越多，`recentN` 取平均越能反映近期节律（相比全量平均对周期漂移更敏感）。

### 4.5 边界与容错

- 同一日期重复标记开始 → 视为修改，更新该记录而非新增。
- 新「开始」与上一次进行中的经期重叠 → 自动闭合上一次（补 `endDate = 新开始日 - 1`）。
- 结束日早于开始日 → 拦截，给出校验提示。
- 未来日期可查看预测，但**不允许记录**（只允许记录今天及过去）。

---

## 5. 数据模型与存储

### 5.1 经期记录（Room）

```kotlin
@Entity(tableName = "period_records")
data class PeriodRecord(
    @PrimaryKey(autoGenerate = true) val id: Long = 0,
    val startDateEpochDay: Long,        // 经期开始日
    val endDateEpochDay: Long?,         // 经期结束日，null = 进行中
    val note: String? = null,           // 备注（可选）
    val createdAt: Long,
    val updatedAt: Long
)
```

> 采用「一次经期 = 一条记录（start + end）」的**片段式建模**，而非逐日打点：
> - 更贴合「开始/结束」的输入心智；
> - 周期计算直接由相邻 `startDate` 相减得到，逻辑简单；
> - 后续若要流量/症状，可另建 `DailyLog` 表挂靠到某天，不影响本表。

### 5.2 用户设置（DataStore Preferences）

```kotlin
data class UserSettings(
    val defaultCycleLength: Int = 28,
    val defaultPeriodLength: Int = 5,
    val recentN: Int = 6
)
```

### 5.3 DAO 关键查询

```kotlin
@Dao
interface PeriodDao {
    @Query("SELECT * FROM period_records ORDER BY startDateEpochDay ASC")
    fun observeAll(): Flow<List<PeriodRecord>>

    @Query("SELECT * FROM period_records WHERE startDateEpochDay = :start")
    suspend fun getByStart(start: Long): PeriodRecord?

    @Query("SELECT * FROM period_records WHERE endDateEpochDay IS NULL")
    suspend fun getOngoing(): PeriodRecord?

    @Insert suspend fun insert(record: PeriodRecord): Long
    @Update suspend fun update(record: PeriodRecord)
    @Delete suspend fun delete(record: PeriodRecord)
}
```

---

## 6. 技术架构

```
Compose Route ──Intent──► MVI ViewModel ──► Repository ──► Data Source
      ▲                    │                    │
      └──UiState/Effect────┘                    │
                                      ┌─────────┼─────────┐
                                      ▼         ▼         ▼
                                    Room    DataStore   纯函数层
```

- **单 Activity + 多 Composable**，严格 MVI；详细约束见 `COMPOSE_MVI_ARCHITECTURE.md`。
- 每个有状态功能使用单一 `UiState`、密封 `Intent`、密封 `Effect` 与唯一 `onIntent` 入口。
- ViewModel 合并 Repository 数据与本地交互状态，产出完整页面 `UiState`（含日历标记、预测结果与选中日期）。
- 预测逻辑独立成 `CyclePredictor`，不依赖 Android，纯 `LocalDate` + `List` 计算，单测覆盖。
- Route 用 `collectAsStateWithLifecycle` 收集状态与一次性 Effect；Screen 只做无状态渲染并回传 Intent。

### 6.1 目录结构（建议）

```
app/src/main/java/com/stephen/pinkdiary/
├── MainActivity.kt
├── PinkdiaryApp.kt                     # 手动 DI 容器
├── data/
│   ├── local/
│   │   ├── PeriodRecord.kt          # Room Entity
│   │   ├── PeriodDao.kt
│   │   └── AppDatabase.kt
│   ├── model/UserSettings.kt
│   ├── repository/                   # Repository 接口 + Room/DataStore 默认实现
│   └── prediction/CyclePredictor.kt  # 纯函数
├── ui/
│   ├── app/                           # 入口 Contract + ViewModel
│   ├── mvi/MviViewModel.kt            # 通用单向状态容器
│   ├── home/                          # Contract + Route/Screen + ViewModel
│   ├── calendar/CalendarMonth.kt, CalendarDayCell.kt, CalendarLegend.kt
│   ├── knowledge/                     # 本地 Markdown 科普 Contract + Route/Screen + ViewModel
│   ├── record/RecordActions.kt
│   ├── settings/                      # Contract + Route/Screen + ViewModel
│   ├── theme/…（现有）
│   └── components/StatusCard.kt
```

---

## 7. UI / UX 设计

### 7.0 科普内容约定

- 首批 15 篇文章覆盖周期基础、初潮、记录与用品、常见症状、缺铁、子宫内膜异位症、PCOS 和围绝经期等主题。
- 标题与摘要位于 `strings.xml`，文章正文位于 `res/raw/`；目录由 `KnowledgeRepository` 统一映射，保持列表与详情数据来源单一。
- 正文基于 WHO、ACOG、FDA、CDC、美国女性健康办公室和 NHS 等权威资料整理，每篇必须保留资料链接、复核日期与非诊疗声明。
- 内容更新时同步核对目录测试、Markdown 展示效果及来源有效性；医疗科普不替代个体化诊断或治疗。

### 7.1 首页布局（单屏为主，设置页次之）

```
┌─────────────────────────────┐
│  状态卡（StatusCard）        │  ← 经期中第 X 天 / 距下次 X 天 / 预测今日开始
│  · 周期第 X 天 · 平均周期 X 天│
├─────────────────────────────┤
│  ◀  2026 年 8 月  ▶         │  ← 月份标题 + 切换
│  一 二 三 四 五 六 日        │  ← 星期表头
│  1  2  3  4  5  6  7        │
│  …  [日历格子，7×6]  …      │
├─────────────────────────────┤
│  ● 经期  ○ 预测经期       今 │  ← 左侧两行图例，右侧独立今天按钮
│  · 卵泡期 · 排卵日 · 黄体期 │
│  ─────────────────────────  │
│  阶段说明 + 紧凑操作按钮    │  ← 一行说明 + 「标记开始/结束」「清除」
└─────────────────────────────┘
```

### 7.2 日历格子状态与配色

| 状态 | 视觉 | 颜色建议 |
|------|------|----------|
| 普通日期 | 无背景 | — |
| 经期日（已记录） | 独立实心小圆 | 高对比莓红 |
| 预测经期日 | 独立淡粉实心小圆 | 淡藕粉 |
| 估算卵泡期 / 排卵日 / 黄体期 | 数字后的浅色小圆 | 低饱和青绿 / 浅黄 / 淡紫，视觉层级低于经期 |
| 今天 | 加粗日期 + 日期下方小圆点 | 主题强调色 |
| 选中日期 | 围绕日期标记的同心圆环 | 主题强调色 |
| 非本月日期 | 淡灰 | `onSurface` 40% 透明度 |

- 日历容器使用中性极浅灰底色，与淡藕粉预测标记保持清晰但柔和的明度差。
- 已结束经期：起止日分别显示独立的高对比小圆，不连成色带。
- 进行中经期：开始日为高对比小圆，后续日到「今天」为浅粉小圆。
- 预测区间：起止日分别显示独立的浅粉描边小圆。

### 7.3 记录交互

1. 点击某天 → 图例下方显示内联 `RecordActions` 操作区；日期由日历选中态表达，操作区不重复显示日期。
2. 操作按钮上方保留一行阶段说明；按优先级显示已记录经期、预测经期或估算卵泡期/排卵日/黄体期。
3. 按钮随上下文变化：
   - 该日无标记 → 「标记经期开始」
   - 该日已标记开始且未结束 → 「标记经期结束」
   - 该日是进行中经期的开始日 → 「标记经期结束」
   - 已存在完整记录 → 「删除这条记录」
4. 快捷入口：首页提供「今天来月经了」一键标记（自动处理开始/进行中逻辑）。

### 7.4 设置页

- 默认周期长度（滑动条 / 步进器，21–45 天）
- 默认经期长度（2–10 天）
- 参与平均的最近周期数（1–12，默认 6）
- 步进器使用圆形实心加减按钮；每次有效调整提供轻量触觉反馈，达到上下限时按钮禁用且不触发反馈。

---

## 8. 依赖清单（已落地版本）

| 依赖 | 版本 | 用途 |
|------|------|------|
| `com.google.devtools.ksp` 插件 | 2.3.6 | Room 注解处理（KSP 2.3.x 起与 Kotlin 编译器版本解耦，AGP 9 内置 Kotlin 需 2.3.1+） |
| `androidx.room:room-runtime` / `room-ktx` / `room-compiler` | 2.8.4 | 持久化（KSP 处理器 `ksp(room-compiler)`） |
| `androidx.datastore:datastore-preferences` | 1.2.1 | 用户设置 |
| `androidx.lifecycle:lifecycle-viewmodel-compose` | 2.11.0 | `viewModel()` 组合函数 |
| `androidx.lifecycle:lifecycle-runtime-compose` | 2.11.0 | `collectAsStateWithLifecycle` |
| `org.jetbrains.kotlinx:kotlinx-coroutines-android` | 1.10.2 | 协程/Flow（显式声明，避免依赖传递不确定性） |
| `androidx.navigation:navigation-compose` | 2.9.8 | 底部导航与页面状态恢复 |
| `com.mikepenz:multiplatform-markdown-renderer-m3` | 0.38.1 | 科普 Markdown 的 Compose Material 3 原生渲染；与项目 Kotlin 2.2 对齐 |

> 说明：本项目采用 AGP 9 的「内置 Kotlin」模型（`com.android.application` 直接编译 Kotlin，无需 `kotlin-android` 插件），KSP 因此选用与编译器版本解耦的 2.3.x 系列而非 `2.2.10-2.0.2`。Room 3.0 已发布但包名重构、KMP 优先，MVP 沿用稳定成熟的 2.8.x。

---

## 9. 迭代里程碑

| 阶段 | 内容 | 交付标准 |
|------|------|----------|
| M1 数据层 | Room + DataStore + Repository + `CyclePredictor` | 单测覆盖预测算法（冷启动/多周期/异常值/进行中） |
| M2 日历 UI | 月视图、切换月份、状态标记、图例 | 可正确渲染经期/预测/估算周期阶段/今天/选中 |
| M3 记录 | 标记开始/结束、编辑/删除、快捷入口 | 记录可持久化并即时反映到日历 |
| M4 预测与状态卡 | 首页状态卡 + 设置页 | 预测随记录实时更新，冷启动可用 |
| M5 打磨 | 空态、校验提示、主题适配、深色模式 | 关键路径无崩溃，交互流畅 |

---

## 10. 风险与注意事项

- **预测准确性**：个体周期波动大，UI 必须弱化「确定性」，用「预计/可能」措辞，避免医疗暗示。
- **隐私**：经期属敏感健康数据。初期本地存储、无网络；后续如做云同步需加密与隐私说明。
- **日期边界**：统一 `epochDay`，避免 `Date`/`Calendar` 时区坑；跨月/跨年渲染需重点测试。
- **可测性**：把日历标记生成与预测计算拆成纯函数，UI 只消费结果。
