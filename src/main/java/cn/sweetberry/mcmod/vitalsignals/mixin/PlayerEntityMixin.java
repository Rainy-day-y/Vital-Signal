package cn.sweetberry.mcmod.vitalsignals.mixin;

import cn.sweetberry.mcmod.vitalsignals.damage.context.DamageContext;
import cn.sweetberry.mcmod.vitalsignals.damage.context.DamageContextHolder;
import net.minecraft.entity.damage.DamageSource;
import net.minecraft.entity.player.PlayerEntity;
import net.minecraft.server.world.ServerWorld;
import net.minecraft.world.Difficulty;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PlayerEntity.class)
public abstract class PlayerEntityMixin {
    @Inject(method = "damage", at = @At("HEAD"))
    private void calcDifficultyReduce(
            ServerWorld world,
            DamageSource source,
            float amount,
            CallbackInfoReturnable<Boolean> cir
    ) {
        if (this instanceof DamageContextHolder holder) {
            DamageContext ctx = holder.fabric_vital_signals_getDamageContext();
            if (ctx.getToken() == null) return;

            float difficultyReduced = 0.0F;
            if (source.isScaledWithDifficulty()) {
                if (world.getDifficulty() == Difficulty.PEACEFUL) {
                    difficultyReduced = amount - 0.0F;
                }

                if (world.getDifficulty() == Difficulty.EASY) {
                    difficultyReduced = amount - Math.min(amount / 2.0F + 1.0F, amount);
                }

                if (world.getDifficulty() == Difficulty.HARD) {
                    difficultyReduced = amount - amount * 3.0F / 2.0F;
                }
            }

            ctx.markDifficultyReduceResolved();
            ctx.setDifficultyReduced(difficultyReduced);
        }
    }
}