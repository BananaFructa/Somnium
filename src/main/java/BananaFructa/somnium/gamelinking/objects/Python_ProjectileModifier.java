package BananaFructa.somnium.gamelinking.objects;

import BananaFructa.somnium.pyinterpreter.objects.Python_Object;

public class Python_ProjectileModifier extends Python_Object {
    public ProjectileModifiersType type;
    public float value;

    public Python_ProjectileModifier(ProjectileModifiersType type, float value) {
        this.type = type;
        this.value = value;
    }

    @Override
    public Python_Object copy() {
        return new Python_ProjectileModifier(type,value);
    }
}
