# AGENTS.md — AI 协作指南

给在本仓库工作的 AI 编码代理的约定、事实与注意事项。

## 项目概览

Pinkdiary 是一款记录与预测女性经期的 Android 应用。单 Activity + Jetpack Compose + 严格 MVI，数据用 Room 持久化、设置用 DataStore，预测算法为纯函数（无 Android 依赖、可单测）。

## Harness 强制工作流

以下约束适用于每一次代码修改，不得跳过：

1. **改代码前必读**：先完整阅读 [`docs/COMPOSE_MVI_ARCHITECTURE.md`](docs/COMPOSE_MVI_ARCHITECTURE.md)，再开始设计或编辑代码。
2. **严格 MVI**：有业务状态的功能必须使用单一 `UiState`、密封 `Intent`、密封 `Effect` 和唯一 `onIntent` 入口；Composable 不得直接读写 Repository/DAO/DataStore，不得绕过 Intent 调用 ViewModel 业务方法。
3. **改代码后检查文档**：逐项检查 `README.md`、`docs/FEATURE_DESIGN.md`、`docs/PERIOD_PREDICTION_RULES.md`、`docs/COMPOSE_MVI_ARCHITECTURE.md` 与本文件是否受影响；需要时必须在同一变更中刷新。
4. **交付必须声明**：最终交付说明必须写明测试结果与“文档检查结果”（列出已更新文档，或说明无需更新及原因）。

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
Compose Route ──► Intent ──► MVI ViewModel ──► Repository ──► Room / DataStore
       ▲                       │                                  └─ 纯函数层
       └──── UiState / Effect ─┘
```

- **严格 MVI**：每个有状态功能只有一个 `StateFlow<UiState>`、一个 `onIntent(Intent)` 入口和至多一个一次性 `Effect` 流
- **Route / Screen 分层**：Route 收集状态与 Effect；Screen 只接收不可变状态和 `onIntent`
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
│   └── repository/            Repository 接口 + Room/DataStore 默认实现
└── ui/
    ├── app/                   应用入口 MVI（引导分流）
    ├── mvi/                   通用 MviViewModel 状态容器
    ├── navigation/            底部导航（PinkdiaryNavHost、Routes）
    ├── home/                  经期主页（Contract、Screen、ViewModel）
    ├── calendar/              日历（Pager/Month/DayCell/Legend/MacaronColors）
    ├── record/                记录弹窗（RecordSheet）
    ├── settings/              设置页（Contract、Screen、ViewModel）
    ├── knowledge/             科普占位页
    ├── onboarding/            首次启动引导
    ├── components/            StatusCard
    └── theme/                 Color、Theme、Type

app/src/test/java/.../         纯逻辑与 MVI ViewModel 单元测试
docs/                          方案与规则文档
```

## 注意事项（重要）

1. **UI 显示字符串必须写在资源文件**（强制）：所有用户可见文案——Compose `Text`、按钮、`contentDescription`、Snackbar/Toast、校验错误提示等——必须写入 `app/src/main/res/values/strings.xml`，用 `stringResource(R.string.xxx)` 引用，禁止在 Kotlin 中硬编码字符串字面量。带占位符的文案用 `%1$d` / `%2$s` 等格式化，调用 `stringResource(R.string.xxx, arg1, arg2)` 传参；星期等成组文案用 `<string-array>`。内部标识（Room 表名/数据库名、DataStore key、导航 route、preference key 等）不属于 UI 文案，**不要**放进 strings.xml。ViewModel 不解析文案；需要用户提示时发出携带字符串资源 id 或类型化错误的 Effect，由 Route 解析（见 `HomeEffect.ShowMessage`）。

2. **增量编译可能误报**：工作目录存在大小写不同的符号链接（`Pinkdiary` → `PinkDiary`），会破坏 Gradle 文件监听，偶发「Unresolved reference」或符号解析错误的假阳性。**遇到奇怪的编译错误时，先 `./gradlew clean` 再构建。**

3. **`combine` 最多 5 个流**：`kotlinx.coroutines.flow.combine` 内置重载只到 5 个 Flow；需要更多时拆成嵌套 combine 或用独立的 `StateFlow`。

4. **Room 走 KSP**：`app/build.gradle.kts` 里用 `ksp(libs.androidx.room.compiler)`；schema 导出到 `app/schemas/`（`exportSchema = true`）。

5. **颜色分两类**：主题主色（`PinkPrimary` 等）用于按钮/控件；日历语义色（`PeriodPink` 等）用于数据标记。改「经期颜色」只动日历语义色，不要动主题主色。

6. **预测/标记是纯函数**：`CyclePredictor`、`CalendarMarks`、`PeriodLogic` 中的规则改动，务必同步更新 `docs/PERIOD_PREDICTION_RULES.md` 和对应单测。

7. **深色模式**：日历标记底色尽量用「主题表面色 + 半透明叠加」或提供深色变体，避免硬编码浅色在深色下不可读。
