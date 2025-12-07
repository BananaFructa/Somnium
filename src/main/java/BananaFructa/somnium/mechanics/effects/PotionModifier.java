package BananaFructa.somnium.mechanics.effects;

import BananaFructa.somnium.gamelinking.objects.PotionModifiersType;

public class PotionModifier {

    PotionModifiersType type;
    float value;

    public PotionModifier(PotionModifiersType modifier) {
        this.type = modifier;
    }

    public PotionModifier(PotionModifiersType modifier, float value) {
        this.type = modifier;
        this.value = value;
    }

}
