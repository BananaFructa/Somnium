package BananaFructa.somnium.mixin;

import BananaFructa.somnium.EventHandler;
import BananaFructa.somnium.gamelinking.objects.PotionModifiersType;
import BananaFructa.somnium.gamelinking.objects.Python_EffectModifier;
import BananaFructa.somnium.mechanics.effects.EffectUtils;
import BananaFructa.somnium.mechanics.effects.ProgrammableEffect;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.ArrayList;
import java.util.stream.Collectors;

@Mixin(LivingEntity.class)
public class LivingEntityEffectImplementations {

    @Inject(method = "getDamageAfterMagicAbsorb", at = @At("RETURN"), cancellable = true)
    public void potionAbsorb(DamageSource p_21193_, float p_21194_, CallbackInfoReturnable<Float> cir) {
        LivingEntity entity = (LivingEntity)(Object)this;
        float rem = cir.getReturnValue();
        float percent = EffectUtils.addEffects(entity,PotionModifiersType.RESISTANCE);
        // It is not an oversight that this heals you if percent > 1
        cir.setReturnValue(rem - percent * rem);
    }

    @Inject(method = "hurt", at = @At("HEAD"), cancellable = true)
    public void hurt(DamageSource damageSource, float p_21017_, CallbackInfoReturnable<Boolean> cir) {
        if (damageSource.is(DamageTypeTags.IS_FIRE)) {
            LivingEntity entity = (LivingEntity)(Object)this;
            if (EffectUtils.hasEffect(entity,PotionModifiersType.FIRE_RESISTANCE)) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "eat", at = @At("HEAD"))
    public void eat(Level level, ItemStack stack, CallbackInfoReturnable<ItemStack> cir) {
        if (stack.isEdible()) {
            EventHandler.entityAte((LivingEntity)(Object)this,stack);
        }
    }

    @Inject(method = "getJumpPower", at = @At("RETURN"), cancellable = true)
    public void getJumpPower(CallbackInfoReturnable<Float> cir) {
        float ret = cir.getReturnValue();
        float percent = EffectUtils.addEffects((LivingEntity)(Object)this,PotionModifiersType.JUMP);
        cir.setReturnValue(ret * (1 + percent));
    }

}
