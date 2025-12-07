package BananaFructa.somnium.mixin;

import BananaFructa.somnium.EventHandler;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.PotionItem;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(PotionItem.class)
public class PotionHook {

    @Inject(method = "finishUsingItem",at=@At("HEAD"))
    private void injected(ItemStack p_42984_, Level p_42985_, LivingEntity p_42986_, CallbackInfoReturnable<ItemStack> cir) {
        /*for (PlayerLoggerOld logger : PlayerLoggerOld.allLoggers) {
            logger.potionDrunk((Player)p_42986_,p_42984_);
        }*/
        EventHandler.potionDrunk(p_42986_,p_42984_);
    }

}
