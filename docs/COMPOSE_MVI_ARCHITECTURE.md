# Pinkdiary Compose + MVI 编码架构规范

> 状态：生效中  
> 适用范围：`app/src/main/java/com/stephen/pinkdiary/` 下的全部新增与修改代码  
> 目标：以单向数据流约束 Compose 页面，让状态可预测、事件可追踪、副作用可测试。

## 1. 架构总览

Pinkdiary 采用单 Activity、Jetpack Compose、严格 MVI、Repository 与手动依赖注入：

```text
Compose Route ──collect──> UiState <──── ViewModel <──── Repository <──── Room / DataStore
      │                                  │                    │
      └──────── UiIntent ───────────────>│                    └── prediction 纯函数
                                         │
Compose Route <──────── UiEffect ─────────┘
```

依赖只能由外向内：`UI -> presentation/ViewModel -> data/domain`。数据层不得依赖 UI；纯函数层不得依赖 Android、Compose 或 IO。

## 2. 严格 MVI 契约

每个有业务状态的功能必须定义同名的三类契约，并由一个 ViewModel 作为唯一状态持有者：

- `XxxUiState`：页面完整、不可变、可渲染的持久状态，使用 `data class`。
- `XxxIntent`：用户或系统输入，使用 `sealed interface`；命名为已经发生的动作，如 `DateSelected`。
- `XxxEffect`：只消费一次的瞬时输出，如 Snackbar、导航、打开系统页面；使用 `sealed interface`。

强制规则：

1. 一个功能只公开一个 `StateFlow<XxxUiState>`、一个 `onIntent(XxxIntent)` 和至多一个 `Flow<XxxEffect>`。
2. Composable 不得直接调用 Repository/DAO/DataStore，也不得直接调用 ViewModel 的业务方法。
3. ViewModel 是业务状态的唯一写入者；状态只能通过原子 reducer 产生新值，禁止暴露 `MutableStateFlow`。
4. Snackbar、Toast、导航等不得塞进 `UiState` 后再用 `consume/reset` 清空，必须走 `UiEffect`。
5. `UiState` 不保存 `Context`、`View`、`NavController`、Compose state 或已解析的用户文案。
6. ViewModel 不持有 Activity/Composable；用户文案由 UI 通过字符串资源解析。Effect 可携带稳定的资源 id 或类型化错误。
7. IO 仅在 `viewModelScope` 内发起。异常必须转成状态或 Effect，不能无处理地使协程失败。
8. 同一 Intent 在相同初始状态与数据输入下应产生可预测结果；复杂计算下沉为纯 reducer/领域函数并单测。

推荐契约：

```kotlin
data class FeatureUiState(
    val isLoading: Boolean = true,
    val data: Data? = null
)

sealed interface FeatureIntent {
    data object RetryClicked : FeatureIntent
}

sealed interface FeatureEffect {
    data class ShowMessage(@param:StringRes val messageRes: Int) : FeatureEffect
}
```

## 3. ViewModel 标准

- 继承项目的 `MviViewModel<Intent, State, Effect>`，通过 `reduce { copy(...) }` 更新状态，通过 `emitEffect(...)` 发出一次性事件。
- Repository 的冷流由 ViewModel 收集并合并到同一个 `UiState`，生命周期由 `viewModelScope` 管理。
- `UiState` 默认值必须合法且可渲染；首次数据尚未返回时用显式 `isLoading`/页面状态表示。
- 需要防重复提交时在状态中建模 `isSubmitting`，并在 reducer 中完成门控。
- 时间是业务输入。对可测试的日期逻辑优先注入 `Clock` 或日期提供函数，不在多个 Composable 中各自调用 `now()`。

## 4. Compose 分层

每个有 ViewModel 的页面拆成两层：

```kotlin
@Composable
fun FeatureRoute(viewModel: FeatureViewModel) {
    val state by viewModel.uiState.collectAsStateWithLifecycle()
    // 收集 Effect，并把 state + onIntent 交给 Screen
}

@Composable
fun FeatureScreen(
    state: FeatureUiState,
    onIntent: (FeatureIntent) -> Unit,
    modifier: Modifier = Modifier
) { /* 纯渲染 */ }
```

- `Route` 负责 ViewModel、生命周期收集、Effect 与导航边界。
- `Screen` 只接收不可变状态与事件函数，便于 Preview、截图测试和复用。
- 可复用组件优先接收最小必要值与语义回调，不向组件层层传递 ViewModel。
- 仅视觉且短生命周期的状态（滚动、Pager、展开动画、焦点）可以 `remember`；业务状态不得只存在于 `remember`。
- 派生值优先在 ViewModel/纯函数生成；只与渲染有关且计算轻量的派生值可留在 UI，必要时用 `remember`/`derivedStateOf`。
- 所有用户可见文案与无障碍描述必须来自 `strings.xml`；不要在 Kotlin 中硬编码。

## 5. 状态、集合与稳定性

- 状态模型使用 `val`、不可变值语义；更新使用 `copy`。
- 不原地修改状态中的 `List`/`Set`/`Map`。Repository 或 reducer 交付新集合实例。
- Lazy 列表提供稳定 key；参数顺序遵循“必需参数、回调、`modifier`、可选参数”。
- Composable 保持小而聚焦；页面布局、状态分支、可复用控件分离，避免把业务判断散落到多个子组件。
- 不为所谓“性能优化”随意添加 `remember`；先保证状态归属正确，再针对可测量问题优化。

## 6. 数据与领域边界

- Repository 是数据访问边界，负责协调 Room/DataStore；DAO/Preferences key 不得泄漏到 UI。
- 预测、日期区间、记录匹配等确定性规则放在纯函数层，并覆盖边界单测。
- 日期统一使用 `LocalDate`；持久化统一使用 `epochDay: Long`。
- 只有确有多个实现或需要隔离外部系统时才引入接口，避免无收益的抽象层。
- 手动 DI 统一由 `PinkdiaryApp` 组装，页面不得自行构造 Repository/DAO。

## 7. 导航与错误处理

- 页面发出类型化导航 Effect，Route/NavHost 执行导航；ViewModel 不持有 `NavController`。
- 可恢复错误映射为 Snackbar 等 Effect；阻塞页面的错误进入 `UiState`。
- 不向用户显示原始异常文本。日志可记录技术信息，UI 只展示字符串资源中的安全文案。
- Effect 使用 `Channel`/非重放 Flow；配置变更后不应重复消费旧 Effect。

## 8. 测试标准

- 纯函数：输入/输出单元测试，覆盖正常、空数据和边界。
- ViewModel：验证 `Intent -> State/Effect`，使用 fake Repository 和可控时间，不依赖 Android UI。
- Compose：关键页面验证 state 分支与 Intent 回传；复杂视觉变更补 Preview 或 UI 测试。
- 修复缺陷必须补能复现缺陷的测试；预测规则变更同步更新 `PERIOD_PREDICTION_RULES.md`。

## 9. 文件组织

```text
ui/<feature>/
├── <Feature>Contract.kt      # UiState / Intent / Effect
├── <Feature>ViewModel.kt     # 状态归并、Intent 处理、副作用调度
└── <Feature>Screen.kt        # Route + stateless Screen + 私有组件

ui/mvi/
└── MviViewModel.kt           # 通用 MVI 状态容器
```

小型无状态页面可不创建空 ViewModel；一旦有业务状态、IO 或一次性 Effect，就必须使用完整契约。

## 10. 变更完成清单

每次代码变更完成前逐项检查：

- [ ] 修改前已阅读本规范，新增代码遵循单一 `UiState` / `Intent` / `Effect`。
- [ ] Compose 未直接访问数据层，Screen 可由 state + callback 独立渲染。
- [ ] 用户文案位于字符串资源，日期与颜色遵循项目约定。
- [ ] 新增或变更的业务规则已有测试。
- [ ] 已运行与风险匹配的构建/测试；奇怪增量错误已用 `clean` 复核。
- [ ] 已检查 `README.md`、`docs/FEATURE_DESIGN.md`、`docs/PERIOD_PREDICTION_RULES.md`、本规范与 `AGENTS.md` 是否需要同步，并完成必要更新。
- [ ] 交付说明明确列出“文档检查结果”（已更新哪些，或为何无需更新）。
