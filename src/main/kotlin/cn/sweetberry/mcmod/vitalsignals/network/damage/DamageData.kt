package cn.sweetberry.mcmod.vitalsignals.network.damage

import cn.sweetberry.mcmod.vitalsignals.VitalSignals
import cn.sweetberry.mcmod.vitalsignals.events.damage.context.DamageContext
import cn.sweetberry.mcmod.vitalsignals.events.damage.context.DamagePhase
import com.mojang.serialization.Codec
import com.mojang.serialization.codecs.RecordCodecBuilder
import net.minecraft.registry.RegistryKeys

data class DamageData(
    val gameVersion: String = "1.21.11",
    val version: Int = 1,

    val isDirect: Boolean? = null,
    val typeId: Int? = null,
    val phase: String = "UNKNOWN",
    val isCancelled: Boolean = false,

    val damageAmount: Float = 0f,
    val rawDamageAmount: Float = 0f,

    // 难度减免
    val difficultyReduced: Float = 0f,
    // 盾牌格挡
    val shieldBlocked: Float = 0f,
    // 冰冻效果减免（对的，增伤就是负的减免）
    val iceReduced: Float = 0f,
    // 头盔防御对头部重伤的减免
    val helmetReduced: Float = 0f,
    // 护甲减免
    val armorReduced: Float = 0f,
    // 效果（除伤害吸收）与附魔减免
    val effectAndEnchantmentReduced: Float = 0f,
    // 伤害吸收
    val absorbed: Float = 0f,
    // 其他未知补差
    val otherReduced: Float = 0f
) {
    // 伤害数据类
    // 不应该发送完成PRE阶段前的数据包!!! 无论做什么用途连基本信息都没记录的数据包都是无意义的
    private constructor(ctx: DamageContext) : this(
        isDirect = ctx.source?.isDirect,
        typeId =
            ctx.world?.registryManager?.getOrThrow(RegistryKeys.DAMAGE_TYPE)?.getRawId(ctx.source?.type),
        phase = ctx.phase.name,
        isCancelled = ctx.canceled,
        damageAmount = ctx.finalDamage,
        rawDamageAmount = ctx.originalDamage,
        difficultyReduced = ctx.difficultyReduced,
        shieldBlocked = ctx.shieldBlocked,
        iceReduced = ctx.iceReduced,
        helmetReduced = ctx.helmetReduced,
        armorReduced = ctx.armorReduced,
        effectAndEnchantmentReduced = ctx.effectAndEnchantmentReduced,
        absorbed = ctx.absorbed,
        otherReduced = ctx.originalDamage - (
                ctx.difficultyReduced + ctx.iceReduced + ctx.helmetReduced +
                        ctx.shieldBlocked + ctx.armorReduced + ctx.effectAndEnchantmentReduced +
                        ctx.absorbed + ctx.finalDamage
                )
    )

    companion object {
        fun fromContext(ctx: DamageContext): DamageData {
            if (ctx.phase == DamagePhase.CREATED && ctx.phase == DamagePhase.RESETTING) {
                VitalSignals.logger.error(
                    "DamageData.fromContext called with phase ${ctx.phase}, cancelling and returning empty DamageData."
                )
                return DamageData()
            }
            if (ctx.source == null) {
                VitalSignals.logger.error(
                    "DamageData.fromContext called with null damage source, cancelling and returning empty DamageData."
                )
                return DamageData()
            }

            return DamageData(ctx)
        }

        val CODEC: Codec<DamageData> =
            RecordCodecBuilder.create { builderInstance: RecordCodecBuilder.Instance<DamageData> ->
                builderInstance.group(
                    Codec.STRING.fieldOf("gameVersion").forGetter(DamageData::gameVersion),
                    Codec.INT.fieldOf("version").forGetter(DamageData::version),

                    Codec.BOOL.fieldOf("isDirect").forGetter { obj: DamageData -> obj.isDirect ?: false },
                    Codec.INT.fieldOf("typeId").forGetter { obj: DamageData -> obj.typeId ?: -1 },
                    Codec.STRING.fieldOf("phase").forGetter(DamageData::phase),
                    Codec.BOOL.fieldOf("isCancelled").forGetter(DamageData::isCancelled),

                    Codec.FLOAT.fieldOf("damageAmount").forGetter(DamageData::damageAmount),
                    Codec.FLOAT.fieldOf("rawDamageAmount").forGetter(DamageData::rawDamageAmount),

                    Codec.FLOAT.fieldOf("difficultyReduced").forGetter(DamageData::difficultyReduced),
                    Codec.FLOAT.fieldOf("shieldBlocked").forGetter(DamageData::shieldBlocked),
                    Codec.FLOAT.fieldOf("iceReduced").forGetter(DamageData::iceReduced),
                    Codec.FLOAT.fieldOf("helmetReduced").forGetter(DamageData::helmetReduced),
                    Codec.FLOAT.fieldOf("armorReduced").forGetter(DamageData::armorReduced),
                    Codec.FLOAT.fieldOf("effectAndEnchantmentReduced")
                        .forGetter(DamageData::effectAndEnchantmentReduced),
                    Codec.FLOAT.fieldOf("absorbed").forGetter(DamageData::absorbed),
                    Codec.FLOAT.fieldOf("otherReduced").forGetter(DamageData::otherReduced)
                ).apply(builderInstance, ::DamageData)
            }
    }
}