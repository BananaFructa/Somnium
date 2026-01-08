package BananaFructa.somnium.mechanics.projectiles;

import BananaFructa.somnium.Somnium;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.level.Level;

import java.util.HashMap;

public class ProgrammableProjectileProvider {

    private static HashMap<String, ProgrammableProjectileInfo> registeredProjectiles = new HashMap<>();

    public static void createProjectile(String projId, String name, String description, String code, String onEntity, String onBlock, String onTick, int lifetime, float R,float G,float B) {
        ProgrammableProjectileInfo projectileInfo = new ProgrammableProjectileInfo(name,description,code,onEntity,onBlock,onTick,lifetime,Somnium.INSTANCE.worldData.getNextProjectileCacheId(),R,G,B);
        registeredProjectiles.put(projId, projectileInfo);
        Somnium.INSTANCE.worldData.setDirty();
    }

    public static ProgrammableProjectile getPulse(String projectileId, Level level, LivingEntity shooter) {
        if (!registeredProjectiles.containsKey(projectileId)) return null;
        ProgrammableProjectileInfo info = registeredProjectiles.get(projectileId);
        return ProgrammableProjectile.make(level,shooter,info);
    }

    public static CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("size",registeredProjectiles.size());
        int index = 0;
        for (String key : registeredProjectiles.keySet()) {
            tag.putString("projectile_key_"+index,key);
            tag.put("projectile_"+index,registeredProjectiles.get(key).write());
            index++;
        }
        return tag;
    }

    public static void read(CompoundTag tag) {
        registeredProjectiles.clear();
        int size = tag.getInt("size");
        for (int i = 0;i < size;i++) {
            String key = tag.getString("projectile_key_"+i);
            ProgrammableProjectileInfo info = ProgrammableProjectileInfo.read(tag.getCompound("projectile_"+i));
            registeredProjectiles.put(key,info);
        }
    }


}
