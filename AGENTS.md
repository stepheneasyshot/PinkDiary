# AGENTS.md — AI 协作指南

给在本仓库工作的 AI 编码代理的约定、事实与注意事项。

## 项目概览

Pinkdiary 是一款记录与预测女性经期的 Android 应用。单 Activity + Jetpack Compose + MVVM，数据用 Room 持久化、设置用 DataStore，预测算法为纯函数（无 Android 依赖、可单测）。

## 常用命令

```bash
./gradlew :app:assembleDebug                 # 编译 Debug APK
./gradlew :app:testDebugUnitTest             # 运行单元测试
./gradlew clean :app:assembleDebug :app:testDebugUnitTest   # 干净构建 + 单测（推荐）
```

> 首次构建需联网下载 Gradle 发行版与依赖，会写入 `~/.gradle`。

## 关键技术事实

- **包名**：`com.stephen.pinkdiary`
- **minSdk 33 / targetSdk 37 / compileSdk 37**
- Kotlin 2.2、AGP 9（内置 Kotlin，**没有**独立的 `kotlin-android` 插件）
- Compose BOM `2026.02.01`、Material 3
- Room 2.8.4 使用 **KSP**（`ksp(...)`，版本 2.3.6），不是 kapt
- Navigation Compose 2.9.8（底部导航）
- DataStore Preferences 1.2.1（设置与引导状态）
- 依赖统一在 `gradle/libs.versions.toml` 管理

## 架构约定

```
UI (Compose) ──► ViewModel (StateFlow) ──► Repository ──► Room / DataStore
                                                              └─ CyclePredictor / CalendarMarks / PeriodLogic（纯函数）
```

- **MVVM**：View 只消费 `StateFlow`，动作通过 ViewModel 方法下发；副作用用 `viewModelScope.launch`
- **手动 DI**：`PinkdiaryApp` 持有 `AppDatabase`、`PeriodRepository`、`UserSettingsRepository`
- **纯函数层**（`data/prediction/`）不依赖 Android / IO，新增逻辑时优先放这里并配单测
- **日期只精确到天**：用 `java.time.LocalDate`；存库用 `epochDay: Long`，避免 `Date`/`Calendar`

## 目录结构

```
app/src/main/java/com/stephen/pinkdiary/
├── MainActivity.kt            入口（引导分流 → PinkdiaryNavHost）
├── PinkdiaryApp.kt            手动 DI 容器
├── data/
│   ├── local/                 Room：PeriodRecord、PeriodDao、AppDatabase
│   ├── model/                 UserSettings
│   ├── prediction/            纯函数：CyclePredictor、CalendarMarks、PeriodLogic
│   └── repository/            PeriodRepository、UserSettingsRepository
└── ui/
    ├── navigation/            底部导航（PinkdiaryNavHost、Routes）
    ├── home/                  经期主页（HomeScreen、HomeViewModel）
    ├── calendar/              日历（Pager/Month/DayCell/Legend/MacaronColors）
    ├── record/                记录弹窗（RecordSheet）
    ├── settings/              设置页（Screen、ViewModel）
    ├── knowledge/             科普占位页
    ├── onboarding/            首次启动引导
    ├── components/            StatusCard
    └── theme/                 Color、Theme、Type

app/src/test/java/.../         单元测试（纯逻辑）
docs/                          方案与规则文档
```

## 注意事项（重要）

1. **增量编译可能误报**：工作目录存在大小写不同的符号链接（`Pinkdiary` → `PinkDiary`），会破坏 Gradle 文件监听，偶发「Unresolved reference」或符号解析错误的假阳性。**遇到奇怪的编译错误时，先 `./gradlew clean` 再构建。**

2. **`combine` 最多 5 个流**：`kotlinx.coroutines.flow.combine` 内置重载只到 5 个 Flow；需要更多时拆成嵌套 combine 或用独立的 `StateFlow`。

3. **Room 走 KSP**：`app/build.gradle.kts` 里用 `ksp(libs.androidx.room.compiler)`；schema 导出到 `app/schemas/`（`exportSchema = true`）。

4. **颜色分两类**：主题主色（`PinkPrimary` 等）用于按钮/控件；日历语义色（`PeriodPink` 等）用于数据标记。改「经期颜色」只动日历语义色，不要动主题主色。

5. **预测/标记是纯函数**：`CyclePredictor`、`CalendarMarks`、`PeriodLogic` 中的规则改动，务必同步更新 `docs/PERIOD_PREDICTION_RULES.md` 和对应单测。

6. **深色模式**：日历标记底色尽量用「主题表面色 + 半透明叠加」或提供深色变体，避免硬编码浅色在深色下不可读。
