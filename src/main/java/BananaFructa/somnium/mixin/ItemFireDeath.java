package BananaFructa.somnium.mixin;

import BananaFructa.somnium.EventHandler;
import BananaFructa.somnium.Utils;
import net.minecraft.tags.DamageTypeTags;
import net.minecraft.world.damagesource.DamageSource;
import net.minecraft.world.entity.item.ItemEntity;
import net.minecraft.world.item.Items;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(ItemEntity.class)
public class ItemFireDeath {

    @Inject(method = "hurt",at = @At("HEAD"))
    public void hurt(DamageSource source, float amount, CallbackInfoReturnable<Boolean> cir) {
        ItemEntity t = (ItemEntity)(Object)this;
        if (!(t.isInvulnerableTo(source) && !(!t.getItem().isEmpty() && t.getItem().is(Items.NETHER_STAR) && source.is(DamageTypeTags.IS_EXPLOSION))) && t.getItem().getItem().canBeHurtBy(source) && !t.level().isClientSide) {
            int health = Utils.readDeclaredField(ItemEntity.class,t,"f_31987_");
            int rem = (int)((float)(health - amount));
            if (rem<= 0) {
                EventHandler.itemBurned(t.getItem());
            }
        }
    }

}
