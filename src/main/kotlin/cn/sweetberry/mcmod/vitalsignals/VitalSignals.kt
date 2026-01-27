package cn.sweetberry.mcmod.vitalsignals

import cn.sweetberry.mcmod.vitalsignals.damage.event.DamageEventBus
import net.fabricmc.api.ModInitializer
import net.fabricmc.fabric.api.networking.v1.PacketByteBufs
import net.fabricmc.fabric.api.networking.v1.ServerPlayNetworking
import net.minecraft.network.packet.CustomPayload
import org.slf4j.LoggerFactory


object VitalSignals : ModInitializer {
    private val logger = LoggerFactory.getLogger("vital-signals")

    override fun onInitialize() {
        // DamageEventBus.register { TDamageLogger.logDamage(it) }
        DamageEventBus.register { context ->
            run {
                logger.info(context.toString())

//                ServerPlayNetworking.send(context.target, buf)
            }
        }
        logger.info("Vital Signals mod initialized.")
    }
}