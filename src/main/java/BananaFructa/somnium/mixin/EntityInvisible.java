package BananaFructa.somnium.mixin;

import BananaFructa.somnium.gamelinking.objects.PotionModifiersType;
import BananaFructa.somnium.mechanics.effects.EffectUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;


@Mixin(Entity.class)
public class EntityInvisible {


    @Inject(method = "isInvisible",at = @At("RETURN"), cancellable = true)
    public void isInvisible(CallbackInfoReturnable<Boolean> cir) {
        if ((Object) this instanceof LivingEntity) {
            if (EffectUtils.hasEffect((LivingEntity) (Object) this, PotionModifiersType.INVISIBLE)) {
                cir.setReturnValue(true);
            }
        }
    }

}
