package BananaFructa.somnium.mixin;

import BananaFructa.somnium.EventHandler;
import net.minecraft.core.BlockPos;
import net.minecraft.core.Direction;
import net.minecraft.util.Tuple;
import net.minecraft.world.level.BlockGetter;
import net.minecraft.world.level.Level;
import net.minecraft.world.level.SignalGetter;
import net.minecraft.world.level.block.state.BlockBehaviour;
import net.minecraft.world.level.block.state.BlockState;
import org.spongepowered.asm.mixin.Mixin;
import org.spongepowered.asm.mixin.Overwrite;
import org.spongepowered.asm.mixin.injection.At;
import org.spongepowered.asm.mixin.injection.Inject;
import org.spongepowered.asm.mixin.injection.callback.CallbackInfoReturnable;

@Mixin(SignalGetter.class)
public interface RedstoneDistruption {

    @Overwrite
    default int getSignal(BlockPos p_277961_, Direction p_277351_) {
        for (Tuple<Level,BlockPos> pos : EventHandler.redstoneDisruptors) {
            if (pos.getB().distSqr(p_277961_) < EventHandler.redstoneDisruptRadius * EventHandler.redstoneDisruptRadius) return 15;
        }
        BlockState blockstate = ((SignalGetter)(Object)this).getBlockState(p_277961_);
        int i = blockstate.getSignal(((SignalGetter)(Object)this), p_277961_, p_277351_);
        return blockstate.shouldCheckWeakPower(((SignalGetter)(Object)this), p_277961_, p_277351_) ? Math.max(i, ((SignalGetter)(Object)this).getDirectSignalTo(p_277961_)) : i;
    }


}
