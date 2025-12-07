package BananaFructa.somnium.mixin;

import BananaFructa.somnium.EventHandler;
import BananaFructa.somnium.gamelinking.GameLinkingHandler;
import net.minecraft.world.InteractionResult;
import net.minecraft.world.item.BlockItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.item.context.UseOnContext;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.function.Function;

@Mixin(ItemStack.class)
public class BlockPlaceHook {

    @Inject(method = "onItemUse",at = @At("HEAD"), cancellable = true, remap = false)
    public void itemUse(UseOnContext p_41662_, Function<UseOnContext, InteractionResult> callback, CallbackInfoReturnable<InteractionResult> cir) {
        ItemStack t = (ItemStack) (Object) this;
        EventHandler.onBlockPlaced(p_41662_.getPlayer(),t);
        if (t.getItem() instanceof  BlockItem && GameLinkingHandler.isSomniumItem(t)) cir.setReturnValue(InteractionResult.FAIL);
    }

}
