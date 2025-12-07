package BananaFructa.somnium.mixin;

import BananaFructa.somnium.EventHandler;
import net.minecraft.core.Holder;
import net.minecraft.world.InteractionHand;
import net.minecraft.world.InteractionResultHolder;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.Instrument;
import net.minecraft.world.item.InstrumentItem;
import net.minecraft.world.item.ItemStack;
import net.minecraft.world.level.Level;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Shadow;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

import java.util.Optional;

@Mixin(InstrumentItem.class)
public abstract class InstrumentPlayHook {

    @Shadow private Optional<? extends Holder<Instrument>> getInstrument(ItemStack p_220135_) {
        throw new AbstractMethodError("Not Shadowed?");
    };

    @Inject(method = "use", at = @At("HEAD"))
    public void use(Level p_220123_, Player p_220124_, InteractionHand p_220125_, CallbackInfoReturnable<InteractionResultHolder<ItemStack>> cir) {
        ItemStack item = p_220124_.getItemInHand(p_220125_);
        Optional<? extends Holder<Instrument>> $$4 = getInstrument(item);
        if ($$4.isPresent()) {
            EventHandler.hoooooooorn(p_220124_,item);
        }
    }

}
