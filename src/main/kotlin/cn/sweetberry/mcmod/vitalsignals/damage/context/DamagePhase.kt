package cn.sweetberry.mcmod.vitalsignals.damage.context

enum class DamagePhase {
    CREATED,        // Context 刚创建
    PRE,            // 初始状态采集（血量 / 吸收 / 装备）
    DIFFICULTY,     // 难度减免
    SHIELD,         // 盾牌格挡
    TYPE,           // 伤害类型相关增减
    PRE_ARMOR,      // 护甲减免前状态采集
    ARMOR,          // 护甲
    PRE_EFFECT,     // 效果 / 附魔减免前状态采集
    EFFECT,         // 药水 / 附魔
    ABSORPTION,     // 伤害吸收
    FINAL,          // 最终伤害已确定
    ENDED,          // 已分发，不可再修改
    RESETTING       // 重置中，仅用于标记状态，不限制写入
}