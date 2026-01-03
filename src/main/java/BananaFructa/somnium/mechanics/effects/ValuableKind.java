package BananaFructa.somnium.mechanics.effects;

import net.minecraft.world.level.block.Block;
import net.minecraft.world.level.block.Blocks;

public enum ValuableKind {
    CHEST(Blocks.CHEST,Blocks.TRAPPED_CHEST,Blocks.ENDER_CHEST),
    LAPIS(Blocks.LAPIS_ORE,Blocks.DEEPSLATE_LAPIS_ORE,Blocks.LAPIS_BLOCK),
    COAL(Blocks.COAL_ORE,Blocks.DEEPSLATE_COAL_ORE,Blocks.COAL_BLOCK),
    COPPER(Blocks.COPPER_ORE,Blocks.DEEPSLATE_COPPER_ORE,Blocks.COPPER_BLOCK,Blocks.WAXED_COPPER_BLOCK),
    IRON(Blocks.IRON_ORE,Blocks.DEEPSLATE_IRON_ORE,Blocks.IRON_BLOCK),
    GOLD(Blocks.GOLD_ORE,Blocks.DEEPSLATE_GOLD_ORE,Blocks.NETHER_GOLD_ORE,Blocks.RAW_GOLD_BLOCK,Blocks.GOLD_BLOCK),
    REDSTONE(Blocks.REDSTONE_ORE,Blocks.DEEPSLATE_REDSTONE_ORE,Blocks.REDSTONE_BLOCK),
    DIAMOND(Blocks.DIAMOND_ORE,Blocks.DEEPSLATE_DIAMOND_ORE,Blocks.DIAMOND_BLOCK),
    NETHERITE(Blocks.ANCIENT_DEBRIS,Blocks.NETHERITE_BLOCK),
    EMERALD(Blocks.EMERALD_ORE,Blocks.EMERALD_BLOCK);

    public Block[] block;
    ValuableKind(Block... b) {
        this.block = b;
    }
}
