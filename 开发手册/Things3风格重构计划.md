# Things 3 风格重构计划

## 总体目标

将 timePlus 从底部 Tab 导航的传统待办应用，重构为 Things 3 风格的侧边栏驱动、动画优雅、快速输入的现代任务管理应用。

## 重构三大阶段

```
阶段一：导航架构重构（侧边栏 + 页面结构）
阶段二：交互动画体验升级（完成动画 / 滑动手势 / 过渡效果）
阶段三：Magic Button 快速输入（底部面板 + 上下文感知）
```

---

## 侧边栏视图结构

```
┌─────────────────────────────────┐
│  timePlus                       │
│  ─────────────────────────────  │
│  ☀ 今天                    (3) │
│  📆 即将到来                (7) │
│  🗂 随时                   (12) │
│  ✅ 已完成                  (5) │
│  ─────────────────────────────  │
│  标签                           │
│    ● 工作                  (4)  │
│    ● 生活                  (3)  │
│    ● 健康                  (2)  │
│  ─────────────────────────────  │
│  ⚙ 设置                        │
└─────────────────────────────────┘
```

### 各视图数据来源

| 视图 | 数据过滤逻辑 |
|------|-------------|
| 今天 | `dueDateTime.toLocalDate() == today` + 当天应显示的周期性任务 |
| 即将到来 | `dueDateTime > today`，按日期分组时间轴展示，含周期性任务 |
| 随时 | `!isCompleted` 的全部任务 |
| 已完成 | `isCompleted == true`，按完成时间倒序 |
| 标签分类 | `tagId == 选中标签ID` 的任务 |

---

## 第一阶段：导航架构重构

### 1.1 定义新的侧边栏导航结构

**修改文件：** `ui/screens/AppNavigation.kt`

- 废弃现有 `NavigationItem` sealed class（底部 Tab 路由）
- 新建 `SidebarDestination` sealed class：
  - `Today` — 今天
  - `Upcoming` — 即将到来
  - `Anytime` — 随时
  - `Completed` — 已完成
  - `TagFilter(tagId: Long, tagName: String)` — 标签筛选
  - `Settings` — 设置

### 1.2 重构主 Scaffold 布局

**修改文件：** `ui/screens/AppNavigation.kt`

- `Scaffold` + `NavigationBar`（底部栏）→ `ModalNavigationDrawer` + `Scaffold`
- 顶部 `TopAppBar`：
  - 左：汉堡菜单按钮（`drawerState.open()`）
  - 中：当前页面标题
  - 右：排序按钮（保留 `sortMode` 功能，仅在"随时"视图显示）
- 删除 `bottomBar` 代码块
- 保留 FAB（第三阶段替换）
- 内容区域根据 `selectedDestination` 渲染对应 Screen

### 1.3 新建侧边栏组件

**新建文件：** `ui/components/SidebarContent.kt`

- `ModalDrawerSheet` 构建侧边栏面板
- 顶部 Header：应用名 "timePlus"
- 五个智能列表项（今天 / 即将到来 / 随时 / 已完成 / 设置）
  - 每项右侧显示未完成任务计数
  - 选中项高亮背景
- 分隔线 + "标签" 标题
- 动态渲染 `allTags`，每项前带标签颜色圆点 + 计数

### 1.4 重构任务行组件 — 卡片改行式

**修改文件：** `ui/components/TodoItemCard.kt` → 重命名为 `TodoItemRow.kt`

- 移除 `Card` 外壳（背景/阴影/圆角）
- 改为纯 `Row` 布局：
  - 左：自定义空心圆勾选框（`Canvas` 绘制，完成时填充+勾号）
  - 中：标题（主行） + 截止时间/标签信息（副行，灰色小字）
  - 右：优先级小圆点指示器（红/橙/绿，替代 Chip）
- 移除行内删除按钮（后续改为滑动手势）
- 行底部添加细分隔线
- 子任务默认折叠，点击展开

### 1.5 新建 "今天" 视图

**修改文件：** `ui/screens/CalendarScreen.kt` → 重构为 `TodayScreen.kt`

- 移除全部日历组件（月/周/日视图切换）
- 顶部大字显示当前日期："3月8日 周日"
- 可选：顶部显示进度摘要（如 "3/8 已完成"）
- 下方直接展示今天的任务列表
- 数据源：`viewModel.todosForToday`（新增，见 1.8）
- 使用新的 `TodoItemRow` 组件

### 1.6 新建 "即将到来" 视图

**新建文件：** `ui/screens/UpcomingScreen.kt`

- 数据源：`viewModel.upcomingTodos`（未来 30 天任务）
- 按日期分组，使用 `LazyColumn` + `stickyHeader` 展示：
  - 日期标题格式："明天 · 周一 3月9日"、"后天 · 周二 3月10日"、"3月15日 周日"
  - 无任务的日期跳过不展示
- 包含周期性任务的合并逻辑（复用 `getTodosByDateRange`）

### 1.7 重构列表页适配侧边栏

**修改文件：** `ui/screens/AllTodosScreen.kt`

- 移除顶部 `FilterMode` 筛选 Chip 和统计卡片
- 改为通用列表组件 `FilteredTodoList`，接收过滤后的数据
- 适配 "随时" / "已完成" / "标签筛选" 三种场景
- "随时"视图保留排序功能（按时间/优先级/标签）
- "已完成"视图按完成时间倒序，行样式置灰 + 删除线

### 1.8 ViewModel 新增数据流

**修改文件：** `viewmodel/TodoViewModel.kt`

新增以下 Flow，现有接口全部保留：

```kotlin
// 今天的任务（固定 LocalDate.now()）
val todosForToday: Flow<List<TodoItem>>

// 即将到来（未来 30 天，按日期分组）
val upcomingTodos: Flow<List<TodoItem>>

// 已完成的任务
val completedTodos: Flow<List<TodoItem>>

// 按标签筛选
fun todosByTag(tagId: Long): Flow<List<TodoItem>>

// 各视图的未完成计数（供侧边栏徽章使用）
val todayCount: Flow<Int>
val upcomingCount: Flow<Int>
val anytimeCount: Flow<Int>
val completedCount: Flow<Int>
fun tagTodoCount(tagId: Long): Flow<Int>
```

### 1.9 重构任务详情 — Dialog 改全屏页面

**修改文件：** `ui/components/TodoDetailDialog.kt` → 重构为 `ui/screens/TodoDetailScreen.kt`

- `AlertDialog` → 全屏 `Scaffold` 页面
- 顶部 `TopAppBar`：返回箭头 + 删除/更多操作
- 内容区域（可滚动 Column）：
  - 标题：大字体无边框 `TextField`，直接内联编辑
  - 属性行（每行一个属性，点击编辑）：
    - 📅 截止日期
    - 🔄 重复规则
    - 🏷 标签
    - ⚡ 优先级
  - 分隔线
  - 子任务列表（`hasSubTasks` 时显示，可添加/勾选/删除）
  - 分隔线
  - 📝 备注（多行无边框 TextField）
  - 打卡记录（周期性任务时显示）
- 导航方式：使用状态管理或 `NavController` 进入/退出

### 1.10 重构设置页

**修改文件：** `ui/screens/ProfileScreen.kt` → 重命名为 `SettingsScreen.kt`

- 从侧边栏底部 "设置" 入口进入
- 内容保持不变（主题/标签管理/数据导入导出/关于）
- 仅调整进入方式：从 Tab 页改为侧边栏触发

### 1.11 清理废弃文件

- 删除 `ui/components/DayCalendarView.kt`（日视图组件）
- 删除 `ui/components/WeekCalendarView.kt`（周视图组件）
- 删除 `ui/components/MonthCalendarView.kt`（月视图组件）
- 删除 `ui/components/TodoList.kt`（旧列表组件，合并到新组件）

### 阶段一执行顺序

```
1.1 定义导航结构
 → 1.2 重构 Scaffold（侧边栏框架搭起来）
 → 1.3 侧边栏组件
 → 1.4 任务行组件（卡片→行式）
 → 1.8 ViewModel 新增数据流
 → 1.5 "今天" 视图
 → 1.6 "即将到来" 视图
 → 1.7 列表页适配
 → 1.9 任务详情全屏页
 → 1.10 设置页调整
 → 1.11 清理废弃文件
```

---

## 第二阶段：交互动画体验升级

### 2.1 自定义勾选动画组件

**新建文件：** `ui/components/AnimatedCheckbox.kt`

- `Canvas` 绘制空心圆
- 点击后：
  1. 圆圈填充动画（`animateFloatAsState(spring)`，`drawArc` 弧度 0→360，300ms）
  2. 勾号从中心扩展出现（`drawPath` + scale 动画）
- 取消完成：反向动画
- 支持优先级颜色：高=红边框，中=橙边框，低=绿边框，默认=灰边框

### 2.2 任务完成动画

**修改文件：** `ui/components/TodoItemRow.kt`

- 勾选时三步动画串联：
  1. 勾选圆圈填充（300ms，spring 弹性）
  2. 标题文字删除线从左到右划出（`animateFloatAsState` 控制宽度比例，200ms）
  3. 任务行向上折叠消失（`AnimatedVisibility` + `shrinkVertically` + `fadeOut`，400ms，delay 600ms）
- 整体时序：勾选→删除线→延迟→消失，总计约 1.2s

### 2.3 列表增删动画

**修改文件：** 各列表页

- 新增任务：`AnimatedVisibility` + `expandVertically(spring(dampingRatio=0.7f))`，从插入位置展开
- 删除任务：`shrinkVertically` + `fadeOut`，平滑折叠
- `LazyColumn` 使用 `animateItem()`（Compose 1.7+）实现重排动画

### 2.4 滑动手势

**新建文件：** `ui/components/SwipeableTaskRow.kt`

- 包裹 `TodoItemRow`，添加滑动操作
- 右滑（露出蓝色背景）：推迟到明天（`dueDateTime` +1 天）
- 左滑（露出红色背景）：删除任务
- 使用 `SwipeToDismissBox`（Material3）实现
- 滑动过程中背景色渐现 + 操作图标

### 2.5 页面切换动画

**修改文件：** `ui/screens/AppNavigation.kt`

- 侧边栏切换视图时：`AnimatedContent` + `fadeIn/fadeOut` + `slideInHorizontally` 过渡
- 进入任务详情页：`slideInHorizontally(initialOffsetX = { it })`（从右滑入）
- 返回列表页：`slideOutHorizontally(targetOffsetX = { it })`（向右滑出）

### 2.6 侧边栏交互动画

**修改文件：** `ui/components/SidebarContent.kt`

- 选中项切换时，高亮背景使用 `animateDpAsState` 平滑移动
- 计数徽章数字变化时 `AnimatedContent` + `slideInVertically` 翻转效果

### 2.7 空状态视图

**新建文件：** `ui/components/EmptyStateView.kt`

- 列表为空时居中显示：图标 + 提示文字
  - 今天："今天没有任务，享受你的一天"
  - 即将到来："没有即将到来的任务"
  - 已完成："还没有已完成的任务"
- 进入动画：`fadeIn` + `scaleIn(spring(dampingRatio=0.6f))`

### 2.8 即将到来 Sticky Header

**修改文件：** `ui/screens/UpcomingScreen.kt`

- 日期标题使用 `stickyHeader`，滚动时吸附顶部
- 标题进出时淡入淡出过渡

### 阶段二执行顺序

```
2.1 自定义勾选组件
 → 2.2 任务完成动画
 → 2.3 列表增删动画
 → 2.4 滑动手势
 → 2.5 页面切换动画
 → 2.6 侧边栏动画
 → 2.7 空状态视图
 → 2.8 Sticky Header
```

---

## 第三阶段：Magic Button 交互

### 3.1 底部快速输入面板

**新建文件：** `ui/components/QuickAddPanel.kt`

- 使用 `ModalBottomSheet` 实现底部滑入面板
- **折叠态（默认）：**
  - 单行输入框：placeholder "新任务..."，自动聚焦弹出键盘
  - 下方一行快捷操作图标按钮：
    - 📅 日期（点击弹出日期选择器）
    - 🏷 标签（点击弹出标签选择器）
    - ⚡ 优先级（点击循环 低→中→高→低）
    - 🔔 通知（开关切换）
  - 右下角：确认按钮
- **展开态（向上拖拽）：**
  - 额外显示：备注输入框、重复规则设置、子任务开关
  - 即 `AddTodoDialog` 完整功能的面板化呈现
- 键盘适配：`imePadding()` 确保不被键盘遮挡
- 回车键：直接创建任务并清空输入框（支持连续快速添加）

### 3.2 Magic Plus 按钮

**修改文件：** `ui/screens/AppNavigation.kt`

- FAB 点击后打开 `QuickAddPanel`
- 上下文感知预设：
  - "今天" 页面 → 日期预设今天
  - "即将到来" 页面 → 日期预设明天
  - "标签筛选" 页面 → 标签预设为当前标签
  - "随时" 页面 → 不预设日期
- FAB 动画：
  - 打开面板时 `+` 旋转 45° 变 `×`（`animateFloatAsState` rotation）
  - 关闭面板时反向旋转回 `+`

### 3.3 抽取可复用表单子组件

**修改文件：** `ui/components/AddTodoDialog.kt`

- `ScrollTimePickerDialog` 抽取为独立文件，供 `QuickAddPanel` 和 `TodoDetailScreen` 复用
- 日期选择器、标签选择器、周期设置等抽取为独立 Composable
- `AddTodoDialog` 整体废弃，全部引用改为 `QuickAddPanel`

### 3.4 清理旧代码

- 移除 `AppNavigation.kt` 中 `AddTodoDialog` 相关的 `showAddDialog` 状态和调用
- 移除 `AddTodoDialog.kt` 文件（逻辑已迁移到 `QuickAddPanel`）

### 阶段三执行顺序

```
3.1 快速输入面板
 → 3.2 Magic Plus 按钮 + 上下文感知
 → 3.3 抽取复用组件
 → 3.4 清理旧代码
```

---

## 涉及文件变更汇总

| 操作 | 文件 | 说明 |
|------|------|------|
| **重构** | `ui/screens/AppNavigation.kt` | 底部 Tab → 侧边栏 + TopAppBar |
| **重构→重命名** | `ui/components/TodoItemCard.kt` → `TodoItemRow.kt` | 卡片 → 行式设计 |
| **重构→重命名** | `ui/screens/CalendarScreen.kt` → `TodayScreen.kt` | 日历页 → "今天"页 |
| **重构** | `ui/screens/AllTodosScreen.kt` | 移除筛选条，适配侧边栏 |
| **重构→重命名** | `ui/screens/ProfileScreen.kt` → `SettingsScreen.kt` | Tab页 → 侧边栏入口 |
| **重构→迁移** | `ui/components/TodoDetailDialog.kt` → `ui/screens/TodoDetailScreen.kt` | 对话框 → 全屏页 |
| **重构→废弃** | `ui/components/AddTodoDialog.kt` | 迁移到 QuickAddPanel 后删除 |
| **修改** | `viewmodel/TodoViewModel.kt` | 新增数据流 |
| **新建** | `ui/components/SidebarContent.kt` | 侧边栏组件 |
| **新建** | `ui/screens/UpcomingScreen.kt` | 即将到来时间轴 |
| **新建** | `ui/components/AnimatedCheckbox.kt` | 勾选动画组件 |
| **新建** | `ui/components/SwipeableTaskRow.kt` | 滑动手势组件 |
| **新建** | `ui/components/QuickAddPanel.kt` | 底部快速添加面板 |
| **新建** | `ui/components/EmptyStateView.kt` | 空状态视图 |
| **删除** | `ui/components/DayCalendarView.kt` | 废弃 |
| **删除** | `ui/components/WeekCalendarView.kt` | 废弃 |
| **删除** | `ui/components/MonthCalendarView.kt` | 废弃 |
| **删除** | `ui/components/TodoList.kt` | 合并到新组件 |
| 不变 | `data/*`、`notification/*`、`export/*`、`utils/*` | 数据层/通知/导出无需修改 |

---

## 设计原则

1. **增量重构：** 每个步骤完成后编译验证，确保可用
2. **数据层不动：** Room 数据库/DAO/Repository/Entity 全部保持不变
3. **ViewModel 只增不删：** 现有接口保留，仅新增 Flow
4. **动画克制：** 仅在关键交互节点（完成/删除/切换）添加动画，不过度装饰
5. **Things 3 精髓：** 信息层级清晰、操作路径短、完成有仪式感
