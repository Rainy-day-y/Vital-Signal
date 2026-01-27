package cn.sweetberry.mcmod.vitalsignals.mixin;

import cn.sweetberry.mcmod.vitalsignals.damage.context.DamageContext;
import cn.sweetberry.mcmod.vitalsignals.damage.context.DamageContextHolder;
import cn.sweetberry.mcmod.vitalsignals.damage.event.DamageEventBus;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.network.ServerPlayerEntity;
import net.minecraft.server.world.ServerWorld;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Unique;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ServerPlayerEntity.class)
public class ServerPlayerEntityMixin implements DamageContextHolder {
    @Unique
    private final DamageContext fabric_vital_signals_damageContext = new DamageContext();

    @Override
    public @NonNull DamageContext fabric_vital_signals_getDamageContext() {
        return this.fabric_vital_signals_damageContext;
    }

    @Inject(method = "damage", at = @At("HEAD"))
    private void onDamageStart(
            @NonNull ServerWorld world,
            @NonNull DamageSource source,
            float amount,
            @NonNull CallbackInfoReturnable<Boolean> cir
    ) {
        DamageContext ctx = this.fabric_vital_signals_getDamageContext();
        ctx.reset();

        ctx.setToken("player_damage_event" + System.nanoTime());
        ctx.setCreatedTime(System.nanoTime());
        ServerPlayerEntity self = (ServerPlayerEntity) (Object) this;
        ctx.markPreResolved();

        ctx.setWorld(world);
        ctx.setTarget(self);
        ctx.setSource(source);
        ctx.setOriginalDamage(amount);
    }

    @Inject(method = "damage", at = @At("RETURN"))
    private void onDamageEnd(
            ServerWorld world,
            DamageSource source,
            float amount,
            @NonNull CallbackInfoReturnable<Boolean> cir
    ) {
        DamageContext ctx = this.fabric_vital_signals_getDamageContext();

        PlayerEntity self = (PlayerEntity) (Object) this;
        float finalAbsorption = self.getAbsorptionAmount();
        float finalHealth = self.getHealth();

        float absorbed = ctx.getPreAbsorption() - finalAbsorption; // 伤害吸收总共减少量
        ctx.markAbsorptionResolved();
        ctx.setAbsorbed(absorbed);

        float damageToHealth = ctx.getPreHealth() - finalHealth;  // 实际扣除的生命值
        ctx.markFinalResolved();
        ctx.setFinalDamage(damageToHealth);

        ctx.end();

        DamageEventBus.INSTANCE.post(ctx);

        ctx.reset();
    }
}
