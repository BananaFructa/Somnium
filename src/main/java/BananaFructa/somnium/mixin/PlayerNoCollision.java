package BananaFructa.somnium.mixin;

import BananaFructa.somnium.gamelinking.objects.PotionModifiersType;
import BananaFructa.somnium.mechanics.effects.EffectUtils;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.phys.Vec3;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(Player.class)
public class PlayerNoCollision {

    @Inject(method = "maybeBackOffFromEdge", at = @At("HEAD"), cancellable = true)
    public void backEdge(Vec3 vec, MoverType p_36202_, CallbackInfoReturnable<Vec3> cir) {
        if ((Object)this instanceof LivingEntity) {
            if (EffectUtils.hasEffect((LivingEntity) (Object) this, PotionModifiersType.PHASING)) {
                cir.setReturnValue(vec);
            }
        }
    }

}
