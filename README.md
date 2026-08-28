# PigThings

PigThings 是一个面向 Minecraft 1.20.1 Forge 整合包的辅助 Mod，当前版本为 `1.5.7`。

## 物品

- **多功能头盔** (`pigthings:nice_helmet`)
  - 提供创造飞行、飞行速度调节、无惯性、夜视、永久饱和、飞行挖掘辅助、保护和磁铁等功能。
  - 佩戴后按 `G` 打开设置界面。

- **矿脉挖掘镐** (`pigthings:vein_mining_pickaxe`)
  - 支持连锁挖矿、掉落物归脚下，以及挖掘范围和间隔设置。

- **多功能戒指** (`pigthings:nice_ring`)
  - 使用 Curios API，可装备到 Curios 饰品槽。
  - 提供与多功能头盔相同的主要功能，设置保存在戒指自身。
  - 头盔和戒指同时装备时，头盔优先生效。

- **障碍破坏者** (`pigthings:nice_pickaxe`)
  - 使用动态小猪材质。
  - 拥有极高的固定挖掘速度，支持所有可破坏方块，基岩类方块除外。
  - 右键每次只能破坏一格，必须松开后再次按下右键才能继续。
  - 按 `G` 可打开设置界面，只提供精准采集开关。
  - 无限耐久且不可破坏。

## 构建

- Minecraft：1.20.1
- Forge：47.4.3
- Java：17

使用以下命令构建：

```text
./gradlew build --no-daemon
```

构建产物输出到 `../Mods/Pigthings Mods`。

## 许可

PigThings 自有源代码使用 MIT License。Minecraft、Minecraft Forge、Curios 以及项目中携带的其他第三方文件遵循各自的许可协议。
