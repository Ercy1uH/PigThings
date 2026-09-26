# 维护说明

## 本地依赖

- `1.20.1/libs/Pigthings-1.4.5.jar`：历史源文件缺失时保留下来的本模组类和资源。构建会合并该 JAR，并让恢复的源码/新资源优先；移除它会丢失功能。
- `1.20.1/libs` Curios 5.9.1 API：Forge 1.20.1 编译依赖。
- `1.21.1/libs` Curios 9.5.1 API 和运行库：NeoForge 1.21.1 开发依赖。
- `1.12.2/libs` Baubles 1.5.2：Forge 1.12.2 编译依赖，游戏运行也需要 Baubles。
- `1.12.2/libs` Ender IO 5.0.49、EnderCore 0.5.78 和从 Ender IO 内提取的 AutoConfig 1.0.2：旅行功能的编译依赖，不打入 PigThings JAR。运行时 Ender IO 为可选依赖，存在时注册实现 `IItemOfTravel` 的唐刀，并复用其 `TravelController`。

1.12.2 的 Ender IO 物品实例化必须隔离在返回 `Item` 的兼容工厂中，不能让事件订阅类直接引用实现 Ender IO 接口的子类，否则缺少 Ender IO 时 JVM 验证会导致启动崩溃。独立传送由 `CarrotSaberTeleport` 在服务端计算，使用原版物品右键数据包与冷却同步。1.7.7 已在不装 Ender IO 的 Forge 14.23.5.2859 隔离客户端/集成服务端中验证启动、16 格传送、冷却、墙体碰撞和普通右键行为。

这些 JAR 已是仓库构建输入，区别于 build/libs 与 dist 内的成品。未更改依赖版本或玩法。

## 美术资源

`assets/reference` 保留用户提供并实际采用的四张参考图，`assets/models` 保存草帽和唐刀的 Blockbench 源模型。游戏读取各版本 src/main/resources 中的导出资源，而不是直接读取 bbmodel。

草帽运行时装备网格额外修正了 Y 轴 180° 朝向，并使用头部显示 Y=7.25、缩放 1.1。原始草帽 bbmodel 作为原稿保存，重新导出时须保留这些修正。

唐刀为兼容 Java 版方块模型制作的简化立体模型，含 200 个方块和 256×256 贴图。原稿细纹未逐像素复刻。

## 发布检查

Forge 1.20.1 的 1.6.5 将液体清晰视野、遗骨幽灵化和唐刀传送移植到现代 API。新增 `pigthings:abilities` 网络通道（协议 1）处理清晰视野开关和主动结束幽灵化，不修改恢复自旧 JAR 的 `pigthings:main` 包结构。重建的 `FlightHelmetScreen` 保留旧磁铁子页面调用接口。客户端/服务端均需安装新版。

1.20.1 的 `META-INF/coremods.json` 注册两个窄范围钩子：给 `LevelRenderer.setupRender` 的旁观渲染参数加入幽灵化条件；给 Ender IO `TravelHandler.canItemTeleport(Player)` 的返回值加入手持唐刀判定，供原有锚点标记和范围逻辑识别。实际游戏模式保持不变。可选兼容类 `EnderIOTravel` 只在检测到末影接口后调用；`libs/EnderIO-1.20.1-6.2.18-beta-all.jar` 仅用于编译，不合并进成品。独立闪现由服务端验证扫过的身体空间、液体、边界及区块加载，再发送位置同步。

1.20.1 幽灵化在 `LivingTickEvent` 和 END player tick 维持 noPhysics 与飞行；能力快照保存在玩家 ForgeData 中，结束、登录、退出和换维度时恢复，结束不修改坐标。底部保护按该维度最小建筑高度加一计算。头盔/戒指处理器和设置包的公开能力应用入口在幽灵化拥有能力时推迟执行，避免永久飞行或无敌残留。

1.6.5 已在 Forge 47.4.10 的两个隔离客户端中实测（Curios + PigThings，以及额外安装 Ender IO 6.2.18-beta）：遗骨 600 tick 持续、200 tick 冷却、立即飞行、穿墙穿地、虚空/岩浆免伤、刷新后恢复、原地解除、客户端空挥请求退出、自然到期、效果期间装备头盔后再摘下无能力残留；唐刀独立闪现和墙前停止、Ender IO 旅行权杖识别与锚点落点；清晰视野中文整行按钮及服务端 NBT 保存、水和岩浆雾范围。封闭石体内的幽灵截图能看到墙后照明洞室，普通生存 HUD 与状态图标保留。测试代码和截图位于 `.buildtest/port120-*`，测试模组不进入发布包。清晰视野不会免疫岩浆伤害。

1.7.10 的头盔液体视野由客户端 `HelmetFluidVision` 处理 Forge `FogDensity` 和 `RenderBlockOverlayEvent`。仅相机玩家头部佩戴本模组头盔且开关开启时生效，水/岩浆使用 EXP 雾模式、密度 0.002；水下遮屏取消，火焰遮屏只在浸入岩浆时取消。空气雾、失明、戒指及伤害规则不受此功能影响。NBT `FlightHelmetClearFluidVision` 缺省为开启；头盔设置包尾部增加布尔字段，联机两端应安装相同版本。幽灵化期间保存头盔设置时推迟重新应用飞行能力，以保留原有能力快照。

已在 Forge 14.23.5.2859 的独立水池/岩浆池中截图对比开关效果，确认远处方块可见、岩浆内火焰遮挡取消、中文设置界面完整；验证旧头盔默认开启、客户端同步、GUI 开/关数据包落到服务端 NBT、摘下头盔恢复普通视野，以及幽灵化期间戴上头盔再修改设置不会在结束并摘下后遗留飞行或无敌。截图与运行报告位于 `.buildtest/screenshots/fluid-*.png` 和 `.buildtest/fluid-*.txt`。

1.12.2 的幽灵化通过 `GhostAbilityHandler` 管理能力快照、免伤与原地退出。1.7.9 按用户要求删除结束时的安全传送及位置记录；结束时不改变坐标，恢复原版碰撞和伤害。`EntityPlayer.onUpdate` 会在 START tick 之后重置 noClip，必须在 `LivingUpdateEvent` 中重新应用，并在 END tick 保持。不要切换旁观模式，否则会隐藏普通生存界面。头盔处理器在幽灵化或待恢复快照存在时暂缓修改飞行，防止恢复顺序导致永久飞行。客户端空挥和被取消的交互通过 `PacketEndGhost` 请求服务端结束效果。

`GhostRenderPlugin` 是 JAR 内置的 1.12.2 Forge 加载插件，在反混淆后给 `RenderGlobal.setupTerrain` 的 spectator 布尔参数加入幽灵化条件。该参数仅参与地形可见性遍历，不改变游戏模式、玩家对象或 HUD。原版身处封闭实心区域时会把可见范围缩为当前区块段，仅设置 noClip 或 renderChunksMany 无法绕过这条分支。幽灵化状态变化时使可见列表失效，以便静止退出时也恢复普通裁剪。加载插件的清单属性必须随发布 JAR 保留，修改后需要重启客户端。

1.7.9 在 Forge 14.23.5.2859 隔离实例中的封闭石块场景实测：普通渲染可见列表为 1 个区块段，幽灵化与旁观渲染均为 5200 个，实际玩家保持生存模式及 HUD。截图验证相邻发光洞穴可见；服务端断言确认墙内解除效果前后 XYZ 完全一致，noClip、临时飞行和无敌移除，窒息伤害恢复。

遗骨贴图来自 `D:/ModWrite/PigThings-main/Image/卡墙猪灵的遗骨1.1.png`，保留透明通道，缩为 256×256。`RemainsFont` 仅对该物品的介绍使用指定 RGB 色值；猪表情由 Segoe UI Emoji 字形栅格化为贴图，避免 1.12.2 字体缺失补充平面字符。状态启用 showParticles，以便原版 HUD 显示状态图标。

1.7.8 已在 Forge 14.23.5.2859、仅装 Baubles 和 PigThings 的隔离客户端及集成服务端中验证：600 tick 持续、200 tick 冷却、立即飞行、真实 tick 后客户端/服务端 noClip、穿墙穿地、虚空及摔落免伤、安全退出、攻击/挖掘退出、客户端空挥退出数据包、自然到期、刷新后能力恢复，以及效果期间戴上头盔后再移除不会留下永久飞行。中文物品名、渐变介绍、状态名称/图标与完整生存 HUD 已截图核验。测试工具和报告位于被忽略的 `.buildtest/`。

1. 构建对应版本，Forge 成品必须完成 reobfJar。
2. 确认 JAR 包含模型、纹理、语言和物品注册；不要用开发类目录替代成品。
3. 游戏内检查左右手、GUI、装备位置、附魔光效、物品功能及服务器加载。

构建缓存、日志、测试运行目录和历史成品不进入 Git。旧本地工程及缓存未删除，整理后的 Git 工程独立保留。

Forge 1.20.1 和 Forge 1.12.2 的唐刀使用本地最新 OBJ 导出及 carrot_saber_obj.png；NeoForge 沿用方块模型。1.12.2 通过 Forge blockstate inventory 变体加载 OBJ，启用 flip-v，握持变换的平移由像素除以 16 转换为方块单位，并显式处理左手镜像。旧版 models/item/carrot_saber.json 必须删除，否则其加载优先级会覆盖 OBJ 入口；删除后须 clean 构建，防止旧资源留在 JAR 中。assets/models 中的唐刀为早期可编辑方块稿，不覆盖 OBJ 导出。
