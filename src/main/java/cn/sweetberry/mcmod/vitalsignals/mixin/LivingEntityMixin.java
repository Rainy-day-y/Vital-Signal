package cn.sweetberry.mcmod.vitalsignals.mixin;

import cn.sweetberry.mcmod.vitalsignals.damage.context.DamageContext;
import cn.sweetberry.mcmod.vitalsignals.damage.context.DamageContextHolder;
import cn.sweetberry.mcmod.vitalsignals.damage.context.DamagePhase;
import net.minecraft.entity.EquipmentSlot;
import net.minecraft.entity.LivingEntity;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.registry.tag.DamageTypeTags;
import net.minecraft.registry.tag.EntityTypeTags;
import net.minecraft.server.world.ServerWorld;
import org.jspecify.annotations.NonNull;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(LivingEntity.class)
public class LivingEntityMixin {

    @Inject(method = "getDamageBlockedAmount", at = @At("RETURN"))
    private void captureShieldBlock(
            ServerWorld world,
            DamageSource source,
            float amount,
            @NonNull CallbackInfoReturnable<Float> cir
    ) {
        if (this instanceof DamageContextHolder self) {
            DamageContext ctx = self.fabric_vital_signals_getDamageContext();
            if (ctx.getToken() == null) return;

            float blocked = cir.getReturnValue();
            if (blocked <= 0) blocked = 0.0F;

            ctx.markShieldResolved();
            ctx.setShieldBlocked(blocked);
        }
    }

    @Inject(
            method = "damage",
            at = @At(
                    value = "INVOKE",
                    target = "Lnet/minecraft/entity/LivingEntity;getDamageBlockedAmount(Lnet/minecraft/server/world/ServerWorld;Lnet/minecraft/entity/damage/DamageSource;F)F",
                    shift = At.Shift.AFTER
            )
    )
    private void calcTypeBasedReduce(
            ServerWorld world,
            DamageSource source,
            float amount,
            @NonNull CallbackInfoReturnable<Boolean> cir
    ) {
        if (this instanceof DamageContextHolder holder) {
            DamageContext ctx = holder.fabric_vital_signals_getDamageContext();
            if (ctx.getToken() == null) return;

            LivingEntity self = (LivingEntity) (Object) this;
            float iceReduced = 0.0F;
            float helmetReduced = 0.0F;
            float base = amount - ctx.getShieldBlocked();

            if (source.isIn(DamageTypeTags.IS_FREEZING) && self.getType().isIn(EntityTypeTags.FREEZE_HURTS_EXTRA_TYPES)) {
                iceReduced = (-base * 4.0F);
                base *= 5.0F;
            }

            if (source.isIn(DamageTypeTags.DAMAGES_HELMET) && !self.getEquippedStack(EquipmentSlot.HEAD).isEmpty()) {
                helmetReduced = (0.25F * base);
            }

            ctx.markTypeResolved();
            ctx.setIceReduced(iceReduced);
            ctx.setHelmetReduced(helmetReduced);
        }
    }

    @Inject(method = "applyArmorToDamage", at = @At("HEAD"))
    private void beforeApplyArmor(
            DamageSource source,
            float amount,
            @NonNull CallbackInfoReturnable<Float> cir
    ) {
        if (this instanceof DamageContextHolder self) {
            DamageContext ctx = self.fabric_vital_signals_getDamageContext();
            if (ctx.getToken() == null) return;

            ctx.markPreArmorResolved();
            ctx.setPreArmor(amount);
        }
    }

    @Inject(method = "applyArmorToDamage", at = @At("RETURN"))
    private void captureArmor(
            DamageSource source,
            float amount,
            @NonNull CallbackInfoReturnable<Float> cir
    ) {
        if (this instanceof DamageContextHolder self) {
            DamageContext ctx = self.fabric_vital_signals_getDamageContext();
            if (ctx.getToken() == null) return;

            if (ctx.getPhase() != DamagePhase.PRE_ARMOR) {
                throw new IllegalStateException("DamageContext phase error: expected PRE_ARMOR, but was " + ctx.getPhase());
            }
            float afterArmor = cir.getReturnValue();
            float preArmor = ctx.getPreArmor();

            ctx.markArmorResolved();
            ctx.setArmorReduced(preArmor - afterArmor);
        }
    }

    @Inject(method = "modifyAppliedDamage", at = @At("HEAD"))
    private void beforeModifyAppliedDamage(
            DamageSource source,
            float amount,
            @NonNull CallbackInfoReturnable<Float> cir
    ) {
        if (this instanceof DamageContextHolder self) {
            DamageContext ctx = self.fabric_vital_signals_getDamageContext();
            if (ctx.getToken() == null) return;

            ctx.markPreEffectResolved();
            ctx.setPreEffectAndEnchantment(amount);
        }
    }

    @Inject(method = "modifyAppliedDamage", at = @At("RETURN"))
    private void captureEffectAndEnchantment(
            DamageSource source,
            float amount,
            @NonNull CallbackInfoReturnable<Float> cir
    ) {
        if (this instanceof DamageContextHolder self) {
            DamageContext ctx = self.fabric_vital_signals_getDamageContext();
            if (ctx.getToken() == null) return;

            if (ctx.getPhase() != DamagePhase.PRE_EFFECT) {
                throw new IllegalStateException("DamageContext phase error: expected PRE_EFFECT, but was " + ctx.getPhase());
            }
            float preReduced = ctx.getPreEffectAndEnchantment();
            float afterReduced = cir.getReturnValue();
            ctx.markEffectResolved();

            ctx.setEffectAndEnchantmentReduced(preReduced - afterReduced);
        }
    }
}
