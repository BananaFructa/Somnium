package BananaFructa.somnium.mechanics.projectiles;

import net.minecraft.nbt.CompoundTag;

public class ProgrammableProjectileInfo {

    public String name;
    public String description;
    public String pythonCodeImplementation = null;
    public String onEntityHitFunctionName = null;
    public String onBlockHitFunctionName = null;
    public String onTickFunction = null;

    public ProgrammableProjectileInfo(String name, String description, String code, String onEntityHit, String onBlockHit, String onTick) {
        this.name = name;
        this.description = description;
        this.pythonCodeImplementation = code;
        this.onEntityHitFunctionName = onEntityHit;
        this.onBlockHitFunctionName = onBlockHit;
        this.onTickFunction = onTick;
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name",name);
        tag.putString("description",description);
        tag.putString("code",pythonCodeImplementation);
        tag.putString("onEntityHit",onEntityHitFunctionName);
        tag.putString("onBlockHit",onBlockHitFunctionName);
        tag.putString("onTick",onTickFunction);
        return tag;
    }

    public static ProgrammableProjectileInfo read(CompoundTag tag) {
        String name = tag.getString("name");
        String description = tag.getString("description");
        String code = tag.getString("code");
        String onEntity = tag.getString("onEntityHit");
        String onBlockHit = tag.getString("onBlockHit");
        String onTick = tag.getString("onTick");
        return new ProgrammableProjectileInfo(name,description,code,onEntity,onBlockHit,onTick);
    }

}
