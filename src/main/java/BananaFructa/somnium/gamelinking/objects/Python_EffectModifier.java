package BananaFructa.somnium.gamelinking.objects;

import BananaFructa.somnium.pyinterpreter.objects.Python_Object;

public class Python_EffectModifier extends Python_Object {

    public PotionModifiersType type;
    public float value;

    public Python_EffectModifier(PotionModifiersType type, float value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public Python_Object copy() {
        return new Python_EffectModifier(type,value);
    }
}
