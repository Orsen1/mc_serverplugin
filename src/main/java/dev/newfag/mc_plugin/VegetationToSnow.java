package dev.newfag.mc_plugin;

import org.bukkit.Chunk;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Snow;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.ChunkLoadEvent;

import java.util.concurrent.ThreadLocalRandom;

public class VegetationToSnow implements Listener {

    private static final double REPLACE_CHANCE = 0.98; // ~95 %

    @EventHandler
    public void onChunkLoad(ChunkLoadEvent e) {
        // работаем только с НОВЫМИ чанками
        if (!e.isNewChunk()) {
            return;
        }

        Chunk c = e.getChunk();
        World w = c.getWorld();

        int minY = w.getMinHeight();
        int maxY = w.getMaxHeight();

        var rnd = ThreadLocalRandom.current();

        for (int cx = 0; cx < 16; cx++) {
            for (int cz = 0; cz < 16; cz++) {

                int y = minY;
                while (y < maxY) {
                    Block b = c.getBlock(cx, y, cz);
                    Material m = b.getType();

                    // не растительность → идём выше
                    if (!UtilsWinter.isVegetation(m)) {
                        y++;
                        continue;
                    }

                    // с шансом 85% превращаем этот ВЕСЬ столбик растительности в снег
                    if (rnd.nextDouble() <= REPLACE_CHANCE) {
                        Block below = b.getRelative(0, -1, 0);

                        if (UtilsWinter.canSupportSnow(below)) {
                            int yBottom = y;
                            int yTop = y;

                            // ищем верх столбика растительности
                            while (yTop + 1 < maxY) {
                                Block above = c.getBlock(cx, yTop + 1, cz);
                                if (!above.isPassable()) {
                                    break;
                                }
                                if (!UtilsWinter.isVegetation(above.getType())) {
                                    break;
                                }
                                yTop++;
                            }

                            // чистим растения от низа до верха
                            for (int yy = yBottom; yy <= yTop; yy++) {
                                Block toClear = c.getBlock(cx, yy, cz);
                                if (UtilsWinter.isVegetation(toClear.getType())) {
                                    toClear.setType(Material.AIR, false);
                                }
                            }

                            // снег только на самом нижнем блоке столбика
                            Block base = c.getBlock(cx, yBottom, cz);
                            base.setType(Material.SNOW, false);
                            Snow snow = (Snow) base.getBlockData();
                            snow.setLayers(1);
                            base.setBlockData(snow, false);

                            y = yTop + 1;
                            continue;
                        }
                    }

                    y++;
                }
            }
        }
    }
}
