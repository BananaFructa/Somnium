package BananaFructa.somnium.mechanics.effects;

import BananaFructa.somnium.gamelinking.GameLinkingHandler;
import BananaFructa.somnium.gamelinking.objects.Python_EffectModifier;
import BananaFructa.somnium.pyinterpreter.objects.Python_List;
import BananaFructa.somnium.pyinterpreter.objects.Python_Object;
import net.minecraft.network.chat.Component;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectCategory;
import net.minecraft.world.entity.Entity;
import net.minecraft.world.entity.LivingEntity;
import net.minecraft.world.entity.ai.attributes.AttributeMap;
import net.minecraft.world.entity.ai.attributes.AttributeModifier;
import net.minecraft.world.entity.ai.attributes.Attributes;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.item.ItemStack;
import org.jetbrains.annotations.Nullable;

import java.util.ArrayList;
import java.util.List;
import java.util.UUID;

public class ProgrammableEffect extends MobEffect {

    private static int idCounter = 0;
    public int id = 0;
    public List<UUID> modifiedUUIDs = new ArrayList<>();

    public String displayName = "No name (Please Report!)";
    public String pythonCodeImplementation = null;
    public String onTickFunctionName = null;

    public List<Python_EffectModifier> effectModifiers = new ArrayList<>();

    public ProgrammableEffect() {
        super(MobEffectCategory.NEUTRAL, 0xff0000);
        this.id = idCounter++;
    }

    public void clear() {
        displayName = "No name (Please Report!)";
    }

    public void set(String name, String pythonCode, String function) {
        this.pythonCodeImplementation = pythonCode;
        this.onTickFunctionName = function;
        this.displayName = name;
    }

    private UUID getUUID(int i) {
        while (i >= modifiedUUIDs.size()) modifiedUUIDs.add(UUID.randomUUID());
        return modifiedUUIDs.get(i);
    }

    float partialSaturation = 0;

    @Override
    public void applyEffectTick(LivingEntity entity, int p_19468_) {
        if (entity.level().isClientSide) return;
        if (pythonCodeImplementation != null && onTickFunctionName != null) {
            this.getAttributeModifiers().clear();
            effectModifiers.clear();;
            Python_Object ret = GameLinkingHandler.runPythonCode("effect_"+id,"effect_"+id,onTickFunctionName,pythonCodeImplementation,entity);
            if (ret instanceof Python_List) {
                Python_List list = (Python_List) ret;
                for (int i = 0;i < list.elements.size();i++) {
                    if (list.elements.get(i) instanceof Python_EffectModifier) {
                        Python_EffectModifier modifier = (Python_EffectModifier) list.elements.get(i);
                        effectModifiers.add(modifier);
                        String uuid = getUUID(i).toString();
                        switch (modifier.type) {
                            case HEALTH: {
                                float amount = modifier.value / 20.0f;
                                entity.heal(amount);
                                break;
                            }
                            case SPEED: {
                                addAttributeModifier(Attributes.MOVEMENT_SPEED, uuid,modifier.value, AttributeModifier.Operation.MULTIPLY_TOTAL);
                                break;
                            }
                            case MINING_SPEED: {
                                addAttributeModifier(Attributes.ATTACK_SPEED, uuid, modifier.value, AttributeModifier.Operation.MULTIPLY_TOTAL);
                                break;
                            }
                            case SATURATION: {
                                if (entity instanceof Player) {
                                    partialSaturation += modifier.value / 20.0f;
                                    int wholePart = (int) Math.floor(partialSaturation);
                                    if (wholePart > 0) {
                                        ((Player) entity).getFoodData().eat(wholePart,0);
                                        partialSaturation -= wholePart;
                                    }
                                }
                            }
                        }
                    }
                }
            }
        }
    }

    @Override
    public void removeAttributeModifiers(LivingEntity p_19469_, AttributeMap p_19470_, int p_19471_) {
        super.removeAttributeModifiers(p_19469_, p_19470_, p_19471_);
    }

    @Override
    public List<ItemStack> getCurativeItems() {
        return new ArrayList<>(); // TODO: When curing with milk the effects dont get removed idk why that is
    }

    @Override
    public boolean isDurationEffectTick(int p_19455_, int p_19456_) {
        return true;
    }

    @Override
    public Component getDisplayName() {
        return Component.literal(displayName);
    }
}
