package dev.newfag.mc_plugin;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;

public final class UtilsWinter {

    private UtilsWinter() {
    }

    public static boolean canSupportSnow(Block below) {
        if (below == null) {
            return false;
        }
        Material m = below.getType();
        BlockData data = below.getBlockData();

        if (!m.isSolid() || below.isPassable()) {
            return false;
        }
        if (data instanceof Waterlogged w && w.isWaterlogged()) {
            return false;
        }
        if (!m.isOccluding()) {
            return false;
        }

        String n = m.name();
        if (n.contains("SLAB") || n.contains("STAIRS") || n.contains("CARPET")
                || n.contains("FENCE") || n.contains("WALL") || n.contains("GLASS")
                || n.contains("PANE") || n.contains("DOOR") || n.contains("TRAPDOOR")
                || n.contains("LEAVES") || n.contains("TORCH") || n.contains("CAMPFIRE")
                || n.contains("BAMBOO")) {
            return false;
        }

        return true;
    }

    public static boolean isExposedOrUnderLeaves(Block topAir, Block support) {
        if (!canSupportSnow(support)) {
            return false;
        }

        int highest = support.getWorld().getHighestBlockYAt(support.getX(), support.getZ());
        if (highest == support.getY()) {
            return true;
        }

        for (int y = topAir.getY(); y <= highest; y++) {
            Block b = topAir.getWorld().getBlockAt(topAir.getX(), y, topAir.getZ());
            if (b.getType().isAir()) {
                continue;
            }
            String n = b.getType().name();
            if (!n.endsWith("LEAVES")) {
                return false;
            }
        }
        return true;
    }

    public static boolean isVegetation(Material m) {
        if (m == null) {
            return false;
        }
        if (!m.isBlock() || m.isOccluding()) {
            return false;
        }

        String n = m.name();
        // базовая растительность
        if (n.contains("FERN")) {
            return true;       // FERN, LARGE_FERN
        }
        if (n.contains("FLOWER")) {
            return true;     // ALLIUM, etc, но многие цветы без "FLOWER", поэтому ещё:
        }
        if (n.endsWith("_SEAGRASS")) {
            return false; // подводное не трогаем
        }
        // конкретные цветы/растения
        switch (m) {
            case SHORT_GRASS, TALL_GRASS,
             FERN, LARGE_FERN,
             DEAD_BUSH,

        // мелкие цветы
             DANDELION, POPPY, BLUE_ORCHID, ALLIUM, AZURE_BLUET,
             RED_TULIP, ORANGE_TULIP, WHITE_TULIP, PINK_TULIP,
             OXEYE_DAISY, CORNFLOWER, LILY_OF_THE_VALLEY,
             TORCHFLOWER, WITHER_ROSE,

        // большие цветы / двойные
             SUNFLOWER, LILAC, ROSE_BUSH, PEONY,

        // кусты / грибы / корешки
             SWEET_BERRY_BUSH,
             BROWN_MUSHROOM, RED_MUSHROOM,
             CRIMSON_FUNGUS, WARPED_FUNGUS,
             CRIMSON_ROOTS, WARPED_ROOTS,
             NETHER_SPROUTS,

        // коврики / сухие листья / лепестки
             LEAF_LITTER, MOSS_CARPET, PINK_PETALS,

        // лианы и подвесное
             VINE,
             CAVE_VINES, CAVE_VINES_PLANT,
             TWISTING_VINES, TWISTING_VINES_PLANT,
             WEEPING_VINES, WEEPING_VINES_PLANT,
             GLOW_LICHEN,
             SPORE_BLOSSOM,
             HANGING_ROOTS,

        // столбики
             BAMBOO, BAMBOO_SAPLING,
             SUGAR_CANE
            -> {return true;}
        }
        return false;
    }
}
