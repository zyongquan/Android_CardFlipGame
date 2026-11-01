# Android 翻牌游戏（实验四）

Android 移动应用开发 实验四作业：一个包含 **3 个 Activity** 的翻牌游戏应用，
采用 **MVC** 分层，界面用 XML 布局（LinearLayout / FrameLayout / ScrollView / RecyclerView），
模型层直接使用实验附件给出的 `Card`、`Deck`、`CardMatchingGame`，并补充了单元测试与 UI 测试。

- 应用标题：**周勇权2024180750**（提交要求：标题为自己的「姓名+学号」）
- 包名：`cn.edu.sicnu.cardgame`
- 语言：Kotlin + XML 布局
- 最低版本：Android 7.0（API 24），编译版本：API 37

## 三个 Activity

1. **MainActivity（主界面）**
   显示头像与姓名、性别、学号、电话、邮件地址、爱好、游戏最高分；
   底部两个按钮分别进入「信息修改」和「翻牌游戏」。
2. **PersonalInfoActivity（信息修改）**
   修改姓名、学号、电话、邮件地址，单选性别，多选爱好；
   点「保存」把结果回传给主界面，点「取消」（或返回键）则不修改数据。
   按实验要求，修改后的个人信息只保存在内存中，不写入文件。
3. **CardGameActivity（翻牌游戏）**
   3 列 × 4 行共 12 张牌，点击翻牌、配对得分，可「重置游戏」重新开一局，
   「退出」/返回键结束本局并把最高分带回主界面。

## MVC 分层

| 层次 | 文件 | 职责 |
| --- | --- | --- |
| Model | `Card.kt`、`Deck.kt`、`CardMatchingGame.kt` | 扑克牌、52 张牌堆、翻牌游戏的规则与得分 |
| Model | `Player.kt`、`PlayerRepository.kt` | 玩家信息模型 + 内存仓库（3 个 Activity 共享同一份数据） |
| View | `res/layout/*.xml`、`CardAdapter.kt` | 界面布局；`CardAdapter` 负责把每张牌的状态渲染成牌背/牌面并播放翻牌动画 |
| Controller | `MainActivity.kt`、`PersonalInfoActivity.kt`、`CardGameActivity.kt` | 接收用户交互、更新模型、刷新界面、Activity 之间回传数据 |

游戏计分规则（与实验附件一致）：

- 每翻开一张牌：`-COST_TO_CHOOSE`（1 分）
- 两张牌点数相同：`4 × MATCH_BONUS`（16 分）并标记为已配对
- 两张牌花色相同：`1 × MATCH_BONUS`（4 分）并标记为已配对
- 两张牌点数、花色都不同：`-MISMATCH_PENALTY`（2 分），先翻开的那张牌重新盖上

为了让每一局都能被「配对清空」，发牌时改成**成对抽取**（先随机抽一张，再补一张同点数的牌）。

## 目录结构

```
CardFlipGame/
├── app/
│   ├── src/main/
│   │   ├── AndroidManifest.xml
│   │   ├── java/cn/edu/sicnu/cardgame/
│   │   │   ├── Card.kt                  # 模型：一张牌
│   │   │   ├── Deck.kt                  # 模型：一副牌
│   │   │   ├── CardMatchingGame.kt      # 模型：翻牌游戏规则
│   │   │   ├── Player.kt                # 模型：玩家信息
│   │   │   ├── PlayerRepository.kt      # 模型：内存中的玩家仓库
│   │   │   ├── MainActivity.kt          # 控制器：主界面
│   │   │   ├── PersonalInfoActivity.kt  # 控制器：信息修改
│   │   │   ├── CardGameActivity.kt      # 控制器：翻牌游戏
│   │   │   └── CardAdapter.kt           # 视图：牌桌适配器 + 翻牌动画
│   │   └── res/
│   │       ├── layout/                  # activity_main / activity_personal_info / activity_card_game / item_card
│   │       ├── drawable/                # 牌背、牌面、已配对、按钮、头像等 drawable
│   │       └── values/                  # strings.xml（全部文案）、colors.xml、themes.xml
│   ├── src/test/                        # 单元测试（JVM）：Card / Deck / CardMatchingGame
│   └── src/androidTest/                 # UI 测试（Espresso）：3 个 Activity
└── README.md
```

## 运行方式

1. 用 Android Studio 打开本工程（`File → Open` 选择 `CardFlipGame` 目录）；
2. 首次打开会自动同步 Gradle（本工程使用 AGP 9.3.3 + Gradle 9.5.0 + 内置 Kotlin 支持）；
3. 选择模拟器或真机，点击 Run 运行。

也可以在命令行执行：

```bash
./gradlew :app:assembleDebug          # 编译 Debug APK
./gradlew :app:testDebugUnitTest      # 运行本地单元测试
./gradlew :app:connectedDebugAndroidTest   # 在已连接的设备/模拟器上运行 UI 测试
```

## 测试

- 本地单元测试（`app/src/test`，JVM）：21 个用例，覆盖模型层 `Card` / `Deck` / `CardMatchingGame`
- 界面测试（`app/src/androidTest`，Espresso）：12 个用例，覆盖 3 个 Activity 的交互

两套测试均全部通过，用例明细见实验报告的「五、测试」一节。

## 作者

周勇权 2024180750 · 计算机科学学院
