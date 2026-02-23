# Checklist

- [x] `spawnFollowerFromNBT` 方法中 `mob.load()` 后调用了 `setPersistenceRequired()`
- [x] `onEntityJoinLevel` 事件中为追随者生物设置了持久性
- [x] `registerFollower` 方法中设置了持久性
- [x] 代码编译无错误
- [x] 释放的生物不会立即消失（需在游戏中测试验证）
