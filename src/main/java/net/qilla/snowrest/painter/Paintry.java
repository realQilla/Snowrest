package net.qilla.snowrest.painter;

import net.minecraft.world.level.block.Block;

public final class Paintry {
    private final int id;
    private final float chance;

    private Paintry(int id, float chance) {
        this.id = id;
        this.chance = chance;
    }

    public static Paintry of(int id, float chance) {
        return new Paintry(id, chance);
    }

    public static Paintry of(int id) {
        return new Paintry(id, 1.0f);
    }

    public static Paintry of(Block block, float chance) {
        return Paintry.of(Block.getId(block.defaultBlockState()), chance);
    }

    public static Paintry of(Block block) {
        return Paintry.of(block, 1.0f);
    }

    public int id() {
        return this.id;
    }

    public float chance() {
        return this.chance;
    }
}