package BananaFructa.somnium.packets;

import BananaFructa.somnium.Somnium;
import BananaFructa.somnium.mechanics.effects.ProgrammableEffect;
import BananaFructa.somnium.mechanics.effects.ProgrammableEffectProvider;
import net.minecraft.client.Minecraft;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.network.FriendlyByteBuf;
import net.minecraftforge.network.NetworkEvent;

import java.util.function.Supplier;

public class S2CEffectUpdate {

    public CompoundTag nbt;

    public S2CEffectUpdate(CompoundTag nbt) {
        this.nbt = nbt;
    }

    public S2CEffectUpdate(FriendlyByteBuf buf) {
        nbt = buf.readNbt();
    }

    public void encode(FriendlyByteBuf buf) {
        buf.writeNbt(nbt);
    }

    public void handle(Supplier<NetworkEvent.Context> contextSupplier) {
        S2CEffectUpdateHandler.handle(this);
    }
}
