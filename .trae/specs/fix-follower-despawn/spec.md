# 修复追随者生物消失问题 Spec

## Why
通过"跟随之杖"GUI界面使用怪物蛋释放的模组生物会在生成后立即消失，即使已经设置了 `setPersistenceRequired()`。这是因为 `mob.load(entityData)` 可能会覆盖持久性设置，且spore模组有自己的消失系统。

## What Changes
- 在 `mob.load()` 之后重新调用 `setPersistenceRequired()` 确保持久性不被覆盖
- 在 `onEntityJoinLevel` 事件中为追随者生物设置持久性作为双重保障
- 确保持久性标签正确写入NBT数据

## Impact
- Affected specs: 追随者生物的生命周期管理
- Affected code: 
  - `FollowerEvents.java` - `spawnFollowerFromNBT` 方法
  - `FollowerEvents.java` - `onEntityJoinLevel` 事件处理
  - `FollowerEvents.java` - `registerFollower` 方法

## ADDED Requirements

### Requirement: 追随者生物持久性保障
系统应确保通过跟随之杖释放的生物永久存在，不会被Minecraft原生消失机制或模组消失系统清除。

#### Scenario: 释放生物后持久存在
- **WHEN** 玩家通过跟随之杖GUI释放生物
- **THEN** 生物应永久存在，不会因距离远离或区块卸载而消失

#### Scenario: 加载保存的NBT数据后保持持久性
- **WHEN** 生物加载保存的NBT数据
- **THEN** 持久性设置应保持有效，不被覆盖

#### Scenario: 区块重新加载后追随者仍存在
- **WHEN** 玩家离开后重新进入区块
- **THEN** 追随者生物应仍在原位置或跟随玩家

## MODIFIED Requirements

### Requirement: 实体生成流程
修改 `spawnFollowerFromNBT` 方法，确保持久性设置在NBT数据加载后仍然有效：
1. 生成实体
2. 设置持久性（第一次）
3. 加载NBT数据
4. **重新设置持久性（关键修复）**
5. 注册追随者

### Requirement: 实体加入世界事件
修改 `onEntityJoinLevel` 事件处理，为已注册的追随者生物设置持久性作为双重保障。
