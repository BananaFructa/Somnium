package BananaFructa.somnium.mixin;

import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.chat.Component;
import net.minecraft.world.item.ItemStack;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemStack.class)
public class ItemStackName {

    @Inject(method = "getHoverName", at = @At("HEAD"), cancellable = true)
    public void getHoverName(CallbackInfoReturnable<Component> cir) {
        ItemStack stack = (ItemStack)(Object)this;
        if (stack.hasTag() && stack.getTag().contains("somnium_item")) {
            CompoundTag tag = (CompoundTag) stack.getTag().get("somnium_item");
            if (tag.contains("name")) cir.setReturnValue(Component.literal(tag.getString("name")));
        }
    }


}
