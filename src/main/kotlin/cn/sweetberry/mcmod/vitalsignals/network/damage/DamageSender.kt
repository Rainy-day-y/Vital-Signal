package cn.sweetberry.mcmod.vitalsignals.network.damage

import cn.sweetberry.mcmod.vitalsignals.VitalSignals
import cn.sweetberry.mcmod.vitalsignals.events.damage.context.DamageContext
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking

object DamageSender {
    fun sendContextToClient(ctx: DamageContext){
        if (ctx.target == null) {
            VitalSignals.logger.error("Attempted to send DamageData to a null target. Aborting sending.")
            return
        }
        val data = DamageData.fromContext(ctx)
        if (data.phase == "UNKNOWN" || data.typeId == null || data.isDirect == null) {
            VitalSignals.logger.error(
                "Attempted to send DamageData with UNKNOWN phase or null typeId/isDirect. Aborting sending."
            )
            return
        }
        ServerPlayNetworking.send(
            // 本方法第1个if块已确保
            ctx.target!!,
            DamageS2CPayload(data),
        )
    }
}