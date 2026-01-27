package cn.sweetberry.mcmod.vitalsignals.damage.context

import net.minecraft.entity.damage.DamageSource
import net.minecraft.server.network.ServerPlayerEntity
import net.minecraft.world.World


class DamageContext {
    // 伤害计算上下文

    // token，标记本次是否需要进入上下文记录
    var token: String? = null
        set(value) {
            requirePhase(DamagePhase.CREATED)
            field = value
        }
    var createdTime: Long? = null
        set(value) {
            requirePhase(DamagePhase.CREATED)
            field = value
        }

    // 处理阶段
    var phase: DamagePhase = DamagePhase.CREATED
        private set

    fun requirePhase(vararg allowed: DamagePhase) {
        check(phase in allowed || phase == DamagePhase.RESETTING) {
            "Illegal write in phase $phase, allowed: ${allowed.joinToString()}"
        }
    }

    fun markPreResolved() {
        advanceIfHigher(DamagePhase.PRE)
    }

    fun markDifficultyReduceResolved() {
        advanceIfHigher(DamagePhase.DIFFICULTY)
    }

    fun markShieldResolved() {
        advanceIfHigher(DamagePhase.SHIELD)
    }

    fun markTypeResolved() {
        advanceIfHigher(DamagePhase.TYPE)
    }

    fun markPreArmorResolved() {
        advanceIfHigher(DamagePhase.PRE_ARMOR)
    }

    fun markArmorResolved() {
        advanceIfHigher(DamagePhase.ARMOR)
    }

    fun markPreEffectResolved() {
        advanceIfHigher(DamagePhase.PRE_EFFECT)
    }

    fun markEffectResolved() {
        advanceIfHigher(DamagePhase.EFFECT)
    }

    fun markAbsorptionResolved() {
        advanceIfHigher(DamagePhase.ABSORPTION)
    }

    fun markFinalResolved() {
        advanceIfHigher(DamagePhase.FINAL)
    }

    private fun advanceIfHigher(next: DamagePhase) {
        if (next.ordinal > phase.ordinal) phase = next
    }

    fun end() {
        check(phase == DamagePhase.FINAL) {
            "Cannot end damage context in phase $phase, must be FINAL"
        }
        phase = DamagePhase.ENDED
    }

    // 受击玩家、所在世界引用，注意修改: PRE
    var world: World? = null
        set(value) {
            requirePhase(DamagePhase.PRE)
            field = value
        }
    var target: ServerPlayerEntity? = null
        set(value) {
            requirePhase(DamagePhase.PRE)
            field = value
            hurtTime = value?.hurtTime ?: 0
            preHealth = value?.health ?: 0f
            preAbsorption = value?.absorptionAmount ?: 0f
        }

    // 受击时间（游戏刻）: PRE
    var hurtTime: Int = 0
        set(value) {
            requirePhase(DamagePhase.PRE)
            field = value
        }

    // 扣减前伤害吸收量: PRE
    var preAbsorption: Float = 0f
        private set

    // 扣减前血量: PRE
    var preHealth: Float = 0f
        private set

    // 伤害来源: PRE
    // 可参见 DamageSource 类及 https://zh.minecraft.wiki/w/%E4%BC%A4%E5%AE%B3
    var source: DamageSource? = null
        set(value) {
            requirePhase(DamagePhase.PRE)
            field = value
        }

    // 初始伤害: PRE
    var originalDamage: Float = 0f
        set(value) {
            requirePhase(DamagePhase.PRE)
            field = value
        }

    // 难度减免: DIFFICULTY
    var difficultyReduced: Float = 0f
        set(value) {
            requirePhase(DamagePhase.DIFFICULTY)
            field = value
        }

    // 盾牌格挡: SHIELD
    var shieldBlocked: Float = 0f
        set(value) {
            requirePhase(DamagePhase.SHIELD)
            field = value
        }

    // 冰冻效果减免（对的，增伤就是负的减免）: TYPE
    var iceReduced: Float = 0f
        set(value) {
            requirePhase(DamagePhase.TYPE)
            field = value
        }

    // 头盔防御对头部重伤的减免: TYPE
    var helmetReduced: Float = 0f
        set(value) {
            requirePhase(DamagePhase.TYPE)
            field = value
        }

    // 护甲减免: ARMOR
    var preArmor: Float = 0f
        set(value) {
            requirePhase(DamagePhase.PRE_ARMOR)
            field = value
        }
    var armorReduced: Float = 0f
        set(value) {
            requirePhase(DamagePhase.ARMOR)
            field = value
        }

    // 效果（除伤害吸收）与附魔减免: EFFECT
    var preEffectAndEnchantment: Float = 0f
        set(value) {
            requirePhase(DamagePhase.PRE_EFFECT)
            field = value
        }
    var effectAndEnchantmentReduced: Float = 0f
        set(value) {
            requirePhase(DamagePhase.EFFECT)
            field = value
        }

    // 伤害吸收: ABSORPTION
    var absorbed: Float = 0f
        set(value) {
            requirePhase(DamagePhase.ABSORPTION)
            field = value
        }

    // 最终伤害: FINAL
    var finalDamage: Float = 0f
        set(value) {
            requirePhase(DamagePhase.FINAL)
            field = value
        }

    // 重置上下文: RESETTING -> CREATED
    fun reset() {
        phase = DamagePhase.RESETTING
        token = null
        createdTime = null

        world = null
        target = null
        hurtTime = 0
        preAbsorption = 0f
        preHealth = 0f
        source = null
        originalDamage = 0f

        difficultyReduced = 0f

        iceReduced = 0f
        helmetReduced = 0f

        shieldBlocked = 0f
        preArmor = 0f
        armorReduced = 0f
        preEffectAndEnchantment = 0f
        effectAndEnchantmentReduced = 0f
        absorbed = 0f

        finalDamage = 0f
        phase = DamagePhase.CREATED
    }
}