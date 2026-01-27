package cn.sweetberry.mcmod.vitalsignals

import cn.sweetberry.mcmod.vitalsignals.damage.context.DamageContext
import cn.sweetberry.mcmod.vitalsignals.damage.context.DamagePhase
import net.minecraft.world.Difficulty
import org.slf4j.LoggerFactory

object TDamageLogger {
    private val logger = LoggerFactory.getLogger("vital-signals")
    private var lastTime = System.nanoTime()

    fun logDamage(context: DamageContext) {
        if (context.phase != DamagePhase.ENDED) return
        val timeSinceLast = (context.createdTime!! - lastTime) / 1_000_000
        logger.info("\n"+
            """Damage Event [${context.token ?: "no-token"}]:
            | 
            | Target: ${context.target?.name?.string ?: "unknown"}
            | Source: ${context.source?.name ?: "unknown"}
            | 
            | Time since last: $timeSinceLast ms 
            | createdTime: ${context.createdTime!! / 1_000_000} ms
            | lastTime: ${lastTime / 1_000_000} ms
            | hurtTime: ${context.hurtTime} ticks
            | 
            | Original Damage: ${context.originalDamage}
            | Difficulty Reduced: ${context.difficultyReduced} (Difficulty: ${context.world?.difficulty
                ?: Difficulty.PEACEFUL})
            | Ice Reduced: ${context.iceReduced}
            | Helmet Reduced: ${context.helmetReduced}
            | Shield Blocked: ${context.shieldBlocked}
            | Armor Reduced: ${context.armorReduced} (Pre-Armor: ${context.preArmor})
            | Effect & Enchantment Reduced: ${context.effectAndEnchantmentReduced} (Pre-Effect & Enchantment: ${context.preEffectAndEnchantment})
            | Absorbed: ${context.absorbed} (Pre-Absorption: ${context.preAbsorption})
            | Final Damage: ${context.finalDamage}
            | Pre-Health: ${context.preHealth}
            | 
            | All reductions sum to: ${
                context.difficultyReduced + context.iceReduced + context.helmetReduced +
                        context.shieldBlocked + context.armorReduced + context.effectAndEnchantmentReduced + context.absorbed
            }
            | Final Damage + All reductions: ${
                context.finalDamage + context.difficultyReduced + context.iceReduced + context.helmetReduced +
                        context.shieldBlocked + context.armorReduced + context.effectAndEnchantmentReduced + context.absorbed
            }
            | Original Damage Check: ${
                if (kotlin.math.abs(
                        context.originalDamage - (
                                context.finalDamage + context.difficultyReduced + context.iceReduced + context.helmetReduced +
                                        context.shieldBlocked + context.armorReduced + context.effectAndEnchantmentReduced + context.absorbed
                        )
                    ) < 0.01f
                ) "PASS" else "FAIL"
            }
            | [End of Damage Event]
            """.trimMargin()
        )
        lastTime = context.createdTime!!
    }
}