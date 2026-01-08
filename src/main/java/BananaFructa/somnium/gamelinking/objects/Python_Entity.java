package BananaFructa.somnium.gamelinking.objects;

import BananaFructa.somnium.pyinterpreter.objects.Python_Object;

import java.util.UUID;

public class Python_Entity extends Python_Object {

    public UUID entityUUID;

    public Python_Entity(UUID uuid) {
        this.entityUUID = uuid;
    }

    @Override
    public Python_Object copy() {
        return new Python_Entity(entityUUID);
    }
}
