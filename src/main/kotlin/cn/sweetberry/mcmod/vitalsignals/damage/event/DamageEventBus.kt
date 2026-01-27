package cn.sweetberry.mcmod.vitalsignals.damage.event

import cn.sweetberry.mcmod.vitalsignals.damage.context.DamageContext
import cn.sweetberry.mcmod.vitalsignals.damage.context.DamagePhase

object DamageEventBus {
    private val listeners = mutableListOf<(DamageContext) -> Unit>()

    fun register(listener: (DamageContext) -> Unit) {
        listeners += listener
    }

    fun post(context: DamageContext) {
        check(context.phase == DamagePhase.ENDED)
        listeners.forEach { it(context) }
    }
}