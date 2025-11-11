package dev.newfag.mc_plugin;

import org.bukkit.Material;
import org.bukkit.block.Block;
import org.bukkit.block.data.BlockData;
import org.bukkit.block.data.Waterlogged;

public final class UtilsWinter {
    private UtilsWinter() {}

    public static boolean canSupportSnow(Block below) {
        if (below == null) return false;
        Material m = below.getType();
        BlockData data = below.getBlockData();

        if (!m.isSolid() || below.isPassable()) return false;
        if (data instanceof Waterlogged w && w.isWaterlogged()) return false;
        if (!m.isOccluding()) return false;

        String n = m.name();
        if (n.contains("SLAB") || n.contains("STAIRS") || n.contains("CARPET") ||
            n.contains("FENCE") || n.contains("WALL") || n.contains("GLASS") ||
            n.contains("PANE") || n.contains("DOOR") || n.contains("TRAPDOOR") ||
            n.contains("LEAVES") || n.contains("TORCH") || n.contains("CAMPFIRE") ||
            n.contains("BAMBOO")) return false;

        return true;
    }

    public static boolean isExposedOrUnderLeaves(Block topAir, Block support) {
        if (!canSupportSnow(support)) return false;

        int highest = support.getWorld().getHighestBlockYAt(support.getX(), support.getZ());
        if (highest == support.getY()) return true;

        for (int y = topAir.getY(); y <= highest; y++) {
            Block b = topAir.getWorld().getBlockAt(topAir.getX(), y, topAir.getZ());
            if (b.getType().isAir()) continue;
            String n = b.getType().name();
            if (!n.endsWith("LEAVES")) {
                return false;
            }
        }
        return true;
    }
}