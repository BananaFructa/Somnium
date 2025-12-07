package BananaFructa.somnium.mechanics.items;

import BananaFructa.somnium.pyinterpreter.objects.Python_Object;
import net.minecraft.nbt.CompoundTag;

public class ProgrammableItem {

    public String name;
    public String description;
    public String minecraftName;
    public String useImplFunctionName; // Function or None
    public String onTickImplFunctionName; // Function or None
    public String pythonCode;
    public boolean hasDamage = false;
    public float damage = 0;
    public int uses = -1;

    public ProgrammableItem(String name, String description, String minecraftName, String code, String useImplFunctionName, String onTickImplFunctionName) {
        this.name = name;
        this.description = description;
        this.minecraftName = minecraftName;
        this.useImplFunctionName = useImplFunctionName;
        this.onTickImplFunctionName = onTickImplFunctionName;
        this.pythonCode = code;
    }

    public CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putString("name",name);
        tag.putString("description",description);
        tag.putString("minecraftName",minecraftName);
        if (useImplFunctionName != null) tag.putString("useImpl",useImplFunctionName);
        if (onTickImplFunctionName != null) tag.putString("ticKImpl",onTickImplFunctionName);
        tag.putString("code",pythonCode);
        tag.putBoolean("hasDamage",hasDamage);
        tag.putFloat("damage",damage);
        tag.putInt("uses",uses);
        return tag;
    }

    public static ProgrammableItem read(CompoundTag tag) {
        String name = tag.getString("name");
        String desc = tag.getString("description");
        String minecraftName = tag.getString("minecraftName");
        String useImpl = null;
        if (tag.contains("useImpl")) useImpl = tag.getString("useImpl");
        String tickImpl = null;
        if (tag.contains("tickImpl")) tickImpl = tag.getString("tickImpl");
        String code = tag.getString("code");
        ProgrammableItem item = new ProgrammableItem(name,desc,minecraftName,code,useImpl,tickImpl);
        boolean hasDamage = tag.getBoolean("hasDamage");
        float damage = tag.getFloat("damage");
        int uses = tag.getInt("uses");
        item.hasDamage = hasDamage;
        item.damage = damage;
        item.uses = uses;
        return item;
    }
}
