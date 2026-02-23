# Tasks

- [x] Task 1: 修复 `spawnFollowerFromNBT` 方法中的持久性设置
  - [x] SubTask 1.1: 在 `mob.load(entityData)` 之后重新调用 `mob.setPersistenceRequired()`
  - [x] SubTask 1.2: 确保持久性设置在NBT数据加载后不被覆盖

- [x] Task 2: 在 `onEntityJoinLevel` 事件中添加双重保障
  - [x] SubTask 2.1: 为检测到的追随者生物调用 `setPersistenceRequired()`
  - [x] SubTask 2.2: 确保区块重新加载后持久性仍然有效

- [x] Task 3: 在 `registerFollower` 方法中添加持久性设置
  - [x] SubTask 3.1: 在注册追随者时设置持久性
  - [x] SubTask 3.2: 确保持久性标签写入PersistentData

# Task Dependencies
- Task 2 和 Task 3 可并行执行
- Task 1 是核心修复，应首先完成
