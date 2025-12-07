package BananaFructa.somnium.mechanics.items;

import BananaFructa.somnium.Somnium;
import BananaFructa.somnium.gamelinking.GameLinkingHandler;
import BananaFructa.somnium.pyinterpreter.objects.Python_NoneType;
import net.minecraft.nbt.CompoundTag;
import net.minecraft.world.item.Item;
import net.minecraft.world.item.ItemStack;

import java.util.HashMap;
import java.util.List;

import static BananaFructa.somnium.gamelinking.LinkedGameDefinitions.getItemFromFreeForm;

public class ProgrammableItemProvider {

    private static HashMap<String, ProgrammableItem> registeredItems = new HashMap<>();

    public static void createItem(String itemId, String name, String description, String minecraftItem, String pythonCode, String useFunction, String tickFunction) {
        ProgrammableItem itemInfo = new ProgrammableItem(name,description,minecraftItem,pythonCode,useFunction,tickFunction);
        registeredItems.put(itemId,itemInfo);
        Somnium.INSTANCE.worldData.setDirty();
    }

    public static ItemStack getItem(String itemId, int quantity) {
        if (!registeredItems.containsKey(itemId)) return null;
        ProgrammableItem item = registeredItems.get(itemId);
        Item i = getItemFromFreeForm(item.minecraftName);
        ItemStack is = new ItemStack(i,quantity);
        is.getOrCreateTag();
        CompoundTag somniumItemTag = new CompoundTag();
        somniumItemTag.putInt("id", GameLinkingHandler.getNextCustomItemId());
        somniumItemTag.putString("code",item.pythonCode);
        somniumItemTag.putString("name",item.name);
        somniumItemTag.putString("description",item.description);
        if (item.uses != -1) {
            somniumItemTag.putInt("uses",item.uses);
        }
        if (item.onTickImplFunctionName != null) {
            somniumItemTag.putString("on_tick_impl",item.onTickImplFunctionName);
        }
        if (item.useImplFunctionName != null) {
            somniumItemTag.putString("on_use_impl",item.useImplFunctionName);
        }
        is.getTag().put("somnium_item",somniumItemTag);
        return is;
    }

    public static CompoundTag write() {
        CompoundTag tag = new CompoundTag();
        tag.putInt("size",registeredItems.size());
        int index = 0;
        for (String key : registeredItems.keySet()) {
            tag.putString("item_key_"+index,key);
            tag.put("item_"+index,registeredItems.get(key).write());
            index++;
        }
        return tag;
    }

    public static List<String> getItems() {
        return registeredItems.keySet().stream().toList();
    }

    public static void read(CompoundTag tag) {
        registeredItems.clear();
        int size = tag.getInt("size");
        for (int i = 0;i < size;i++) {
            String key = tag.getString("item_key_"+i);
            ProgrammableItem item = ProgrammableItem.read((CompoundTag) tag.get("item_"+i));
            registeredItems.put(key,item);
        }
    }

}
