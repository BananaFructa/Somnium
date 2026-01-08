package BananaFructa.somnium.mechanics.projectiles;

import net.minecraft.nbt.CompoundTag;

public class ProgrammableProjectileInfo {

    public String name;
    public String description;
    public String pythonCodeImplementation = null;
    public String onEntityHitFunctionName = null;
    public String onBlockHitFunctionName = null;
    public String onTickFunction = null;
    public float R,G,B;
    public int lifetime;
    public long projectileCacheId;

    public ProgrammableProjectileInfo(String name, String description, String code, String onEntityHit, String onBlockHit, String onTick, int lifetime, long id, float R, float G, float B) {
        this.name = name;
        this.description = description;
        this.pythonCodeImplementation = code;
        this.onEntityHitFunctionName = onEntityHit;
        this.onBlockHitFunctionName = onBlockHit;
        this.onTickFunction = onTick;
        this.lifetime = lifetime;
        this.projectileCacheId = id;
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name",name);
        tag.putString("description",description);
        tag.putString("code",pythonCodeImplementation);
        tag.putFloat("r",R);
        tag.putFloat("g",G);
        tag.putFloat("b",B);
        if (onEntityHitFunctionName != null) tag.putString("onEntityHit",onEntityHitFunctionName);
        if (onBlockHitFunctionName != null) tag.putString("onBlockHit",onBlockHitFunctionName);
        if (onTickFunction != null) tag.putString("onTick",onTickFunction);
        tag.putInt("lifetime",lifetime);
        tag.putLong("id",projectileCacheId);
        return tag;
    }

    public static ProgrammableProjectileInfo read(CompoundTag tag) {
        String name = tag.getString("name");
        String description = tag.getString("description");
        String code = tag.getString("code");
        String onEntity = null;
        if (tag.contains("onEntityHit")) {
            onEntity = tag.getString("onEntityHit");
        }
        String onBlockHit = null;
        if (tag.contains("onBlockHit")) {
            onBlockHit = tag.getString("onBlockHit");
        }
        String onTick = null;
        if (tag.contains("onTick")) {
            onTick = tag.getString("onTick");
        }
        float R = 1,G = 1,B = 1;
        if (tag.contains("R")) R = tag.getFloat("R");
        if (tag.contains("G")) G = tag.getFloat("G");
        if (tag.contains("B")) B = tag.getFloat("B");
        int lifetime = tag.getInt("lifetime");
        long id = tag.getLong("id");
        return new ProgrammableProjectileInfo(name,description,code,onEntity,onBlockHit,onTick,lifetime,id,R,G,B);
    }

}
