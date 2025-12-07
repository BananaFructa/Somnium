package BananaFructa.somnium.mixin;

import BananaFructa.somnium.gamelinking.objects.PotionModifiersType;
import BananaFructa.somnium.mechanics.effects.EffectUtils;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.MoverType;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.Level;
import net.minecraft.world.phys.AABB;
import net.minecraft.world.phys.Vec3;
import net.minecraft.world.phys.shapes.VoxelShape;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfo;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@Mixin(Entity.class)
public class EntityNoClip {

    @Inject(method = "isInWall", at = @At("HEAD"), cancellable = true)
    public void inWall(CallbackInfoReturnable<Boolean> cir) {
        if ((Object)this instanceof LivingEntity) {
            if (EffectUtils.hasEffect((LivingEntity)(Object)this, PotionModifiersType.PHASING)) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "onGround", at = @At("HEAD"), cancellable = true)
    public void onGround(CallbackInfoReturnable<Boolean> cir) {
        if ((Object)this instanceof LivingEntity) {
            if (EffectUtils.hasEffect((LivingEntity)(Object)this, PotionModifiersType.PHASING)) {
                cir.setReturnValue(false);
            }
        }
    }

    @Inject(method = "tick", at = @At("HEAD"))
    public void tick(CallbackInfo ci) {
        if ((Object)this instanceof LivingEntity) {
            boolean spectator = false;
            if ((Object)this instanceof Player) {
                spectator = ((Player)(Object)this).isSpectator();
            }
            if (!spectator) ((LivingEntity)(Object)this).noPhysics = (EffectUtils.hasEffect((LivingEntity) (Object) this, PotionModifiersType.PHASING));
        }
    }


}
