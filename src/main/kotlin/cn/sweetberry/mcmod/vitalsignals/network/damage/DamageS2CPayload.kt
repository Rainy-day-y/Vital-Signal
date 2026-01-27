package cn.sweetberry.mcmod.vitalsignals.network.damage

import cn.sweetberry.mcmod.vitalsignals.VitalSignals
import io.netty.handler.codec.EncoderException
import net.minecraft.nbt.NbtCompound
import net.minecraft.nbt.NbtOps
import net.minecraft.nbt.NbtSizeValidationException
import net.minecraft.network.PacketByteBuf
import net.minecraft.network.codec.PacketCodec
import net.minecraft.network.packet.CustomPayload
import net.minecraft.util.Identifier

@JvmRecord
data class DamageS2CPayload(val data: DamageData) : CustomPayload {
    override fun getId(): CustomPayload.Id<out CustomPayload> {
        return ID
    }

    companion object {
        val SUMMON_LIGHTNING_PAYLOAD_ID: Identifier =
            Identifier.of(VitalSignals.MOD_ID, "damage_event_s2c")

        val ID = CustomPayload.Id<DamageS2CPayload>(SUMMON_LIGHTNING_PAYLOAD_ID)

        fun toBuf(payload: DamageS2CPayload, buf: PacketByteBuf) {
            if (payload.data.phase == "UNKNOWN") {
                VitalSignals.logger.error("Attempted to serialize DamageData with UNKNOWN phase. Aborting serialization.")
                return
            }

            val nbtData = DamageData.CODEC.encodeStart(NbtOps.INSTANCE, payload.data)
            if (!nbtData.error().isEmpty) {
                VitalSignals.logger.error(
                    "Failed to serialize DamageData for DamageS2CPayload: ${nbtData.error().get().message()}"
                )
                return
            }

            VitalSignals.logger.debug("Successfully serialized DamageData for DamageS2CPayload.")
            buf.writeNbt(nbtData.result().get())
        }

        fun fromBuf(buf: PacketByteBuf): DamageS2CPayload {
            var dataNbt: NbtCompound? = null
            try {
                dataNbt = buf.readNbt()
            } catch (e: Exception) {
                when (e) {
                    is NbtSizeValidationException, is EncoderException -> {
                        VitalSignals.logger.error("Failed to read NBT from DamageS2CPayload: ${e.message}")
                    }
                    else -> {
                        throw e
                    }
                }
            }
            if (dataNbt == null) {
                VitalSignals.logger.error("DamageS2CPayload contained no NBT data.")
                return DamageS2CPayload(DamageData())
            }
            val decodedData = DamageData.CODEC.decode(NbtOps.INSTANCE, dataNbt)
            if (!decodedData.error().isEmpty) {
                VitalSignals.logger.error(
                    "Failed to deserialize DamageData for DamageS2CPayload: ${decodedData.error().get().message()}"
                )
                return DamageS2CPayload(DamageData())
            }

            VitalSignals.logger.debug("Successfully deserialized DamageData for DamageS2CPayload.")
            return DamageS2CPayload(decodedData.result().get().first)
        }

        val CODEC: PacketCodec<PacketByteBuf, DamageS2CPayload> = PacketCodec.of(
            ::toBuf,
            ::fromBuf
        )
    }
}