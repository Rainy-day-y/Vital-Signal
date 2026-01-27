package cn.sweetberry.mcmod.vitalsignals

import cn.sweetberry.mcmod.vitalsignals.events.damage.DamageEvent
import cn.sweetberry.mcmod.vitalsignals.network.damage.DamageData
import cn.sweetberry.mcmod.vitalsignals.network.damage.DamageS2CPayload
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.networking.v1.PayloadTypeRegistry
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import org.slf4j.Logger
import org.slf4j.LoggerFactory


object VitalSignals : ModInitializer {
    const val MOD_ID = "vital-signals"
    val logger: Logger = LoggerFactory.getLogger("vital-signals")

    override fun onInitialize() {
        val payloadTypeRegistry = PayloadTypeRegistry.playS2C()
        payloadTypeRegistry.register(
            DamageS2CPayload.ID,
            DamageS2CPayload.CODEC
        )

        // DamageEvent.register(TDamageLogger::logDamage)
        DamageEvent.register { run {
            ServerPlayNetworking.send(
                it.target!!,
                DamageS2CPayload(DamageData.fromContext(it)),
            )
        } }
        logger.info("Vital Signals mod initialized.")
    }
}