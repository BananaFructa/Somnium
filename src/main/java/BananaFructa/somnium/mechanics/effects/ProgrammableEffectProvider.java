package BananaFructa.somnium.mechanics.effects;

import BananaFructa.somnium.Somnium;
import net.minecraft.world.effect.MobEffect;
import net.minecraftforge.eventbus.api.IEventBus;
import net.minecraftforge.registries.DeferredRegister;
import net.minecraftforge.registries.ForgeRegistries;
import net.minecraftforge.registries.RegistryObject;

import java.util.ArrayList;
import java.util.HashMap;
import java.util.List;

public class ProgrammableEffectProvider {

    public DeferredRegister<MobEffect> effects = DeferredRegister.create(ForgeRegistries.MOB_EFFECTS, Somnium.MODID);
    public HashMap<String,Integer> effectNaming = new HashMap<>();

    List<RegistryObject<MobEffect>> effectPool = new ArrayList<>();
    public int index = 0;

    public ProgrammableEffectProvider(int effectCount) {
        for (int i = 0;i < effectCount;i++) {
            effectPool.add(effects.register("somnium_programmable_" + i,ProgrammableEffect::new));
        }
    }

    public void register(IEventBus iEventBus) {
        effects.register(iEventBus);
    }

    public void createEffect(String effectId, String name, String pythonCode, String functionImplementation) {
        effectNaming.put(effectId,index);
        MobEffect effect = effectPool.get(index).get();
        ((ProgrammableEffect)effect).set(name,pythonCode,functionImplementation);
        index++;
        if (index == effectPool.size()) index = 0;
        Somnium.INSTANCE.worldData.setDirty();
    }

    public MobEffect getEffect(String effectId) {
        if (!effectNaming.containsKey(effectId)) return null;
        return effectPool.get(effectNaming.get(effectId)).get();
    }

    public MobEffect getEffect(int effectIndex) {
        return effectPool.get(effectIndex).get();
    }

}
