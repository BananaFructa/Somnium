package BananaFructa.somnium;

import BananaFructa.somnium.mechanics.effects.ProgrammableEffect;
import BananaFructa.somnium.mechanics.effects.ProgrammableEffectProvider;
import BananaFructa.somnium.mechanics.items.ProgrammableItemProvider;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.server.level.ServerPlayer;
import net.minecraft.world.level.saveddata.SavedData;

import java.util.HashMap;
import java.util.List;
import java.util.UUID;

public class SomniumWorldData extends SavedData {

    private int lastItemId = 0;
    private CompoundTag localStorage = new CompoundTag();
    private HashMap<UUID,Long> interactionTimers = new HashMap<>();

    public int getCurrentItemId() {
        return lastItemId;
    }

    public int getNextItemId() {
        setDirty();
        return lastItemId++;
    }

    public boolean canInteract(ServerPlayer player) {
        if (interactionTimers.containsKey(player.getUUID()) && player.level().getGameTime() - interactionTimers.get(player.getUUID()) < Config.minInteractionPeriod) return false;
        interactionTimers.put(player.getUUID(),player.level().getGameTime());
        setDirty();
        return true;
    }

    public void tryCreateEntry(String key) {
        if (!localStorage.contains(key)) localStorage.put(key,new CompoundTag());
    }

    public void localStoreInt(String callerKey, String key, int n) {
        tryCreateEntry(callerKey);
        setDirty();
        ((CompoundTag)localStorage.get(callerKey)).putInt(key,n);
    }

    public int localGetInt(String callerKey, String key, int def) {
        tryCreateEntry(callerKey);
        if (((CompoundTag)localStorage.get(callerKey)).contains(key)) {
            return ((CompoundTag)localStorage.get(callerKey)).getInt(key);
        } else return def;
    }

    // TODO: the effect stuff should be split such that each class is serializable

    @Override
    public CompoundTag save(CompoundTag tag) {
        tag.putInt("last_item_id",lastItemId);
        tag.put("somnium_local_data",localStorage);
        tag.put("somnium_item_data", ProgrammableItemProvider.write());

        // Save effects
        CompoundTag effectData = new CompoundTag();
        ProgrammableEffectProvider effectProvider = Somnium.INSTANCE.effectProvider;
        int numberRegisteredEffects = effectProvider.effectNaming.size();
        effectData.putInt("number_registered_effects",numberRegisteredEffects);
        effectData.putInt("effect_end_index",effectProvider.index);
        List<String> effectRegisterKeys = effectProvider.effectNaming.keySet().stream().toList();
        for (int i = 0;i < effectRegisterKeys.size();i++) {
            CompoundTag e = new CompoundTag();
            e.putString("key",effectRegisterKeys.get(i)); //
            e.putInt("position",effectProvider.effectNaming.get(effectRegisterKeys.get(i))); //
            ProgrammableEffect effect = (ProgrammableEffect) effectProvider.getEffect(effectRegisterKeys.get(i));
            e.putString("name",effect.displayName);
            e.putString("code",effect.pythonCodeImplementation);
            e.putString("tick_impl_function",effect.onTickFunctionName);
            e.putInt("uuid_count",effect.modifiedUUIDs.size());
            for (int j = 0;j < effect.modifiedUUIDs.size();j++) {
                e.putUUID("uuid_"+j,effect.modifiedUUIDs.get(j));
            }
            effectData.put("effect_"+i,e);
        }
        tag.put("somnium_effect_data",effectData);

        tag.put("somnium_session_context",Somnium.INSTANCE.ollamaService.writeNBT());

        tag.putInt("player_timer_count",interactionTimers.size());
        List<UUID> keys = interactionTimers.keySet().stream().toList();
        for (int i = 0;i < keys.size();i++) {
            tag.putUUID("player_timer_uuid_"+i,keys.get(i));
            tag.putLong("player_timer_time_"+i,interactionTimers.get(keys.get(i)));
        }

        return tag;
    }

    public static SomniumWorldData load(CompoundTag tag) {
        SomniumWorldData data = new SomniumWorldData();
        if (tag.contains("last_item_id")) {
            data.lastItemId = tag.getInt("last_item_id");
        }
        if (tag.contains("somnium_local_data")) {
            data.localStorage = (CompoundTag) tag.get("somnium_local_data");
        }
        if (tag.contains("somnium_item_data")) {
            ProgrammableItemProvider.read((CompoundTag) tag.get("somnium_item_data"));
        }
        if (tag.contains("somnium_effect_data")) {
            CompoundTag effectData = (CompoundTag) tag.get("somnium_effect_data");
            int effectCount = effectData.getInt("number_registered_effects");
            ProgrammableEffectProvider effectProvider = Somnium.INSTANCE.effectProvider;
            effectProvider.effectNaming.clear();
            effectProvider.index = effectData.getInt("effect_end_index");
            for (int i = 0;i < effectCount;i++) {
                CompoundTag e = (CompoundTag) effectData.get("effect_"+i);
                String key = e.getString("key");
                int position = e.getInt("position");
                effectProvider.effectNaming.put(key,position);
                ProgrammableEffect effect = (ProgrammableEffect) effectProvider.getEffect(key);
                effect.displayName = e.getString("name");
                effect.pythonCodeImplementation = e.getString("code");
                effect.onTickFunctionName = e.getString("tick_impl_function");
                effect.modifiedUUIDs.clear();
                int uuidCount = e.getInt("uuid_count");
                for (int j = 0;j < uuidCount;j++) {
                    effect.modifiedUUIDs.add(e.getUUID("uuid_"+j));
                }
            }
        }
        if (tag.contains("somnium_session_context")) {
            Somnium.INSTANCE.ollamaService.readNBT(tag.getCompound("somnium_session_context"));
        }
        if (tag.contains("player_timer_count")) {
            int size = tag.getInt("player_timer_count");
            for (int i = 0;i < size;i++) {
                UUID key = tag.getUUID("player_timer_uuid_"+i);
                Long time = tag.getLong("player_timer_time_"+i);
                data.interactionTimers.put(key,time);
            }
        }
        return data;
    }
}
