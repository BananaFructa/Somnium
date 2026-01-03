package BananaFructa.somnium.mechanics.effects;

import BananaFructa.somnium.gamelinking.objects.PotionModifiersType;
import BananaFructa.somnium.gamelinking.objects.Python_EffectModifier;
import net.minecraft.world.effect.MobEffect;
import net.minecraft.world.effect.MobEffectInstance;
import net.minecraft.world.entity.LivingEntity;

import java.util.ArrayList;
import java.util.Iterator;
import java.util.stream.Collectors;

public class EffectUtils {

    public static ProgrammableEffect getEffectWithType(LivingEntity entity, PotionModifiersType type) {
        for (Iterator<MobEffectInstance> iterator = entity.getActiveEffects().iterator(); iterator.hasNext();) {
            MobEffect effect = iterator.next().getEffect();
            if (effect instanceof ProgrammableEffect) {
                for (Python_EffectModifier modifier : ((ProgrammableEffect) effect).effectModifiers) {
                    if (modifier == null) return null; // This may happen in case it is called from antoher thread
                    if (modifier.type == type) {
                        return (ProgrammableEffect)effect;
                    }
                }
            }
        }
        return null;
    }

    public static boolean hasEffect(LivingEntity entity, PotionModifiersType type) {
        for (Iterator<MobEffectInstance> iterator = entity.getActiveEffects().iterator(); iterator.hasNext();) {
            MobEffect effect = iterator.next().getEffect();
            if (effect instanceof ProgrammableEffect) {
                for (Python_EffectModifier modifier : ((ProgrammableEffect) effect).effectModifiers) {
                    if (modifier == null) return false; // This may happen in case it is called from antoher thread
                    if (modifier.type == type) {
                        return true;
                    }
                }
            }
        }
        return false;
    }

    public static float addEffects(LivingEntity entity, PotionModifiersType type) {
        float total = 0;
        for (MobEffect effect : entity.getActiveEffects().stream().map((i)->i.getEffect()).collect(Collectors.toCollection(ArrayList::new))) {
            if (effect instanceof ProgrammableEffect) {
                for (Python_EffectModifier modifier : ((ProgrammableEffect) effect).effectModifiers) {
                    if (modifier.type == type) {
                        total += modifier.value;
                    }
                }
            }
        }
        return total;
    }

}
