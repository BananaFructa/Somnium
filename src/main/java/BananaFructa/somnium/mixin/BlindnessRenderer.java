package BananaFructa.somnium.mixin;

import BananaFructa.somnium.Utils;
import BananaFructa.somnium.gamelinking.objects.PotionModifiersType;
import BananaFructa.somnium.mechanics.effects.EffectUtils;
import net.minecraft.client.renderer.FogRenderer;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraftforge.api.distmarker.Dist;
import net.minecraftforge.api.distmarker.OnlyIn;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.List;

@OnlyIn(Dist.CLIENT)
@Mixin(FogRenderer.class)
public class BlindnessRenderer {

    private static Object blindFunction = null;

    @Inject(method = "getPriorityFogFunction", at = @At("HEAD"), cancellable = true)
    private static void fogFunction(Entity entity, float p_234167_, CallbackInfoReturnable<Object> cir) {
        if (blindFunction == null) blindFunction = ((List<?>)Utils.readDeclaredField(FogRenderer.class,null,"f_234164_")).get(0);
        if (entity instanceof LivingEntity) {
            LivingEntity livingEntity = (LivingEntity) entity;
            if (EffectUtils.hasEffect(livingEntity, PotionModifiersType.BLIND)) {
                cir.setReturnValue(blindFunction);
            }
        }
    }

}
