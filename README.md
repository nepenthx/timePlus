# 待办日历应用

一个功能完整的Android待办事项管理应用，支持日历视图和周期性任务管理。

## 功能特性

### 📅 多种日历视图

- **月视图**：以月历形式展示所有待办，点击日期查看当天的待办事项
- **周视图**：横向显示一周的日期，每个日期显示待办数量
- **日视图**：聚焦单日，详细展示当天的所有待办

### ✅ 待办管理

- **添加待办**：支持设置标题、备注、截止时间
- **优先级**：三档优先级（高/中/低），用不同颜色标识
- **完成状态**：勾选完成后文字会显示删除线
- **删除功能**：长按或点击删除按钮删除待办

### 🔄 周期性待办

- **重复类型**：支持每天、每周、每月重复
- **打卡功能**：周期性待办可以每天打卡
- **打卡记录**：查看历史打卡记录和打卡天数统计

### 🎨 现代化UI

- 使用Jetpack Compose构建
- Material Design 3设计语言
- 流畅的动画和交互体验
- 支持深色模式

## 技术栈

- **UI框架**：Jetpack Compose
- **数据库**：Room
- **架构**：MVVM (ViewModel + Repository)
- **异步处理**：Kotlin Coroutines + Flow
- **依赖注入**：无（使用简单的工厂模式）

## 项目结构

```
app/src/main/java/com/nepenthx/timer/
├── data/                      # 数据层
│   ├── Priority.kt           # 优先级枚举
│   ├── RecurringType.kt      # 周期类型枚举
│   ├── TodoItem.kt           # 待办实体
│   ├── CheckInRecord.kt      # 打卡记录实体
│   ├── Converters.kt         # Room类型转换器
│   ├── TodoDao.kt            # 待办数据访问对象
│   ├── CheckInDao.kt         # 打卡记录数据访问对象
│   ├── AppDatabase.kt        # Room数据库
│   └── TodoRepository.kt     # 数据仓库
├── viewmodel/                 # ViewModel层
│   └── TodoViewModel.kt      # 主ViewModel
├── ui/                        # UI层
│   ├── components/           # 可复用组件
│   │   ├── MonthCalendarView.kt    # 月历视图
│   │   ├── WeekCalendarView.kt     # 周视图
│   │   ├── DayCalendarView.kt      # 日视图
│   │   ├── TodoList.kt             # 待办列表
│   │   ├── TodoItemCard.kt         # 待办卡片
│   │   ├── AddTodoDialog.kt        # 添加待办对话框
│   │   └── TodoDetailDialog.kt     # 待办详情对话框
│   ├── screens/              # 屏幕
│   │   └── MainScreen.kt     # 主屏幕
│   └── theme/                # 主题
└── utils/                     # 工具类
    └── DateUtils.kt          # 日期工具

```

## 使用说明

### 添加待办

1. 点击右下角的"+"浮动按钮
2. 填写待办信息（标题必填）
3. 选择时间、优先级、是否重复
4. 点击"确定"保存

### 查看待办

- 在日历上点击日期可以查看该日的待办
- 使用顶部按钮切换月/周/日视图
- 点击待办卡片查看详细信息

### 完成待办

- 勾选待办卡片左侧的复选框
- 已完成的待办会显示删除线

### 周期性待办打卡

1. 点击周期性待办打开详情
2. 点击"打卡"按钮完成今日打卡
3. 查看打卡记录和累计天数

### 删除待办

- 点击待办卡片右侧的删除图标
- 确认删除操作

## 编译和运行

1. 使用Android Studio打开项目
2. 同步Gradle依赖
3. 连接Android设备或启动模拟器
4. 点击"Run"按钮

### 最低要求

- Android SDK 29 (Android 10)
- 目标SDK 36
- Kotlin 2.0.21

## 数据存储

所有数据使用Room数据库存储在本地，包括：

- 待办事项信息
- 打卡记录

数据会持久化保存，即使关闭应用也不会丢失。

## 后续改进建议

- [ ] 添加通知提醒功能
- [ ] 支持待办分类/标签
- [ ] 添加搜索功能
- [ ] 支持数据导出/导入
- [ ] 添加统计图表
- [ ] 支持自定义主题颜色
- [ ] 添加桌面小部件
- [ ] 支持云端同步

## 许可证

本项目仅供学习和个人使用。
