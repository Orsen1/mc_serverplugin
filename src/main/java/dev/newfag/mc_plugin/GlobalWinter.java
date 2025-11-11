package dev.newfag.mc_plugin;

import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

public class GlobalWinter implements Listener {

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        processChunk(e.getChunk());
    }

    public static void processChunk(org.bukkit.Chunk c) {
        World w = c.getWorld();
        int minY = w.getMinHeight();
        int maxY = w.getMaxHeight();

        for (int x = 0; x < 16; x++) {
            for (int z = 0; z < 16; z++) {
                // 1) Найти поверхность: сверху вниз до первого НЕ воздуха
                int y = maxY - 1;
                while (y > minY && w.getBlockAt(c.getX() * 16 + x, y, c.getZ() * 16 + z).getType().isAir()) {
                    y--;
                }
                Block surface = w.getBlockAt(c.getX() * 16 + x, y, c.getZ() * 16 + z);

                // 2) Если верх — вода → замораживаем
                if (surface.getType() == Material.WATER) {
                    // только верхний уровень/источник: ставим ICE
                    if (surface.getBlockData() instanceof Levelled lvl && lvl.getLevel() == 0) {
                        surface.setType(Material.ICE, false);
                    } else {
                        surface.setType(Material.ICE, false);
                    }
                    continue;
                }

                // 3) Положить снежный слой, если сверху нет твёрдого блока
                Block above = surface.getRelative(0, 1, 0);

                // только если сверху AIR и низ подходит
                if (above.getType().isAir() && UtilsWinter.canSupportSnow(surface)) {
                    above.setType(Material.SNOW, false);
                    var snow = (org.bukkit.block.data.type.Snow) above.getBlockData();
                    snow.setLayers(1); // стартовый тонкий слой
                    above.setBlockData(snow, false);
                }
            }
        }
    }
}
