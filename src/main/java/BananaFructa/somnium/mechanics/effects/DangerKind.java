package BananaFructa.somnium.mechanics.effects;

import net.minecraft.world.entity.monster.Monster;
import net.minecraft.world.entity.player.Player;
import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

import java.util.List;

public enum DangerKind {
    MOBS(new Class[]{Monster.class},new Block[]{}),
    PLAYER(new Class[]{Player.class}, new Block[]{}),
    LAVA(new Class[]{},new Block[]{Blocks.LAVA, Blocks.LAVA_CAULDRON}),
    DRIPSTONE(new Class[]{},new Block[]{Blocks.POINTED_DRIPSTONE}),
    SILVERFISH(new Class[]{},new Block[]{Blocks.INFESTED_COBBLESTONE,Blocks.INFESTED_DEEPSLATE,Blocks.INFESTED_STONE,Blocks.INFESTED_STONE_BRICKS,Blocks.INFESTED_CHISELED_STONE_BRICKS,Blocks.INFESTED_CRACKED_STONE_BRICKS,Blocks.INFESTED_MOSSY_STONE_BRICKS}),
    TNT(new Class[]{},new Block[]{Blocks.TNT, Blocks.LIGHTNING_ROD}),
    TRIPWIRE(new Class[]{},new Block[]{Blocks.TRIPWIRE, Blocks.TRIPWIRE_HOOK}),
    DISPENSER(new Class[]{},new Block[]{Blocks.DISPENSER});

    public final Class<?>[] entitiesKind;
    public final Block[] blocks;

    DangerKind(Class<?>[] entityTypes, Block[] blocks) {
        this.entitiesKind = entityTypes;
        this.blocks = blocks;
    }
}
