package Storm;

import org.bukkit.Location;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.entity.Player;

/**
 *
 * @author 0rsen
 */
public class PlayerData {

    public boolean hasLight(Player p) {
        var b = p.getLocation().getBlock();

        int sky = b.getLightFromSky();      // свет от неба
        int block = b.getLightFromBlocks(); // свет от факелов и т.п.

        return block > sky + 1;
    }

    public boolean isSkyVisible(Player p) {
        return p.getLocation().getBlock().getLightFromSky() == 0;
    }

    public static int getEnvironmentCode(Player p) {
        Location loc = p.getLocation();
        World world = loc.getWorld();
        Block center = loc.getBlock();
        boolean noSkyR3 = noSkyInRadius(world, loc, 3);
        double wallFactor3 = wallFactor(world, loc, 3); // [0..1]
        boolean noSkyR1 = noSkyInRadius(world, loc, 1);
        boolean noWallsR1 = countWallsImmediate(world, loc) == 0;
        boolean noSkyR5 = noSkyInRadius(world, loc, 5);

        boolean goodLight = hasGoodLight(center);

        // 1) если в радиусе 5 от игрока не будет видно неба, то return 1
        if (noSkyR5) {
            return 1;
        }

        // 2 / 3) логика для радиуса 3
        if (noSkyR3 && wallFactor3 >= 0.4) {
            return 2;
        }
        
        
        if (noSkyR1 && wallFactor3 >= 0.4) {
            return 2;
        }
        // "если радиус меньше но с каждой стороны есть стена, то return 2"
        // + "если радиус 3, не видно неба и стены не везде, и есть неплохое освещение, то return 2"

        // "если радиус 3 блока, не видно неба и стены не везде, ну типо есть выход на поверхность, то return 3"
        if (noSkyR3 || noSkyR1) {
            return 2;
        }

        // 4 / 5) логика для радиуса 1, без стен

        if (noSkyR1 && noWallsR1) {
            // "если радиус 1, не видно неба и нету стен и есть неплохое освещение, то return 5"
            if (goodLight) {
                return 5;
            }
            // "если радиус 1, не видно неба и нету стен, то return 4"
            return 4;
        }

        // 6) "если все if сверху не сработали, но есть неплохой свет, то return 6"
        if (goodLight) {
            return 6;
        }

        // 7) else return 7
        return 7;
    }

    // === СВЕТ ===
    // "неплохое освещение" — пороги можно менять
    private static boolean hasGoodLight(Block b) {
        int block = b.getLightFromBlocks(); // факелы, лампы и т.п.

        // Подбери под себя:
        return block >= 7;
    }

    private static double coveredSkyFactor(World world, Location loc, int radius) {
        int baseX = loc.getBlockX();
        int baseY = loc.getBlockY();
        int baseZ = loc.getBlockZ();

        int total = 0;
        int covered = 0;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                total++;

                int x = baseX + dx;
                int z = baseZ + dz;

                if (isSkyBlockedInColumn(world, x, baseY, z)) {
                    covered++;
                }
            }
        }

        if (total == 0) {
            return 0.0;
        }
        return covered / (double) total;
    }

    // === НЕБО / КРЫША ===
    // true, если во всём квадрате [-radius, radius] по X/Z на уровне игрока везде skyLight == 0
    private static boolean noSkyInRadius1(World world, Location loc, int radius) {
        int baseX = loc.getBlockX();
        int baseY = loc.getBlockY();
        int baseZ = loc.getBlockZ();

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                Block b = world.getBlockAt(baseX + dx, baseY, baseZ + dz);
                if (b.getLightFromSky() > 0) {
                    return false; // где-то небо видно
                }
            }
        }
        return true; // нигде не видно неба
    }

    private static boolean noSkyInRadius(World world, Location loc, int radius) {
    int baseX = loc.getBlockX();
    int baseY = loc.getBlockY();
    int baseZ = loc.getBlockZ();

    for (int dx = -radius; dx <= radius; dx++) {
        for (int dz = -radius; dz <= radius; dz++) {

            int x = baseX + dx;
            int z = baseZ + dz;

            // Если в ЭТОЙ колонке небо видно — значит в радиусе есть открытое небо
            if (!isSkyBlockedInColumn(world, x, baseY, z)) {
                return false;
            }
        }
    }

    // Мы обошли все колонки, и везде небо было закрыто
    return true;
}

    private static boolean isSkyBlockedInColumn(World world, int x, int startY, int z) {
        int maxY = world.getMaxHeight();

        for (int y = startY + 1; y <= maxY; y++) {
            var b = world.getBlockAt(x, y, z);

            // Если блок НЕ проходится (камень, дерево, крыша и т.п.) – он закрывает небо
            if (!b.isPassable()) {
                return true; // небо заблокировано
            }
        }

        // До самого верха ни одного блока – небо видно
        return false;
    }

    // === СТЕНЫ ===
    // Доля "стен" (непроходимых блоков) вокруг на уровне игрока
    private static double wallFactor(World world, Location loc, int radius) {
        int baseX = loc.getBlockX();
        int baseY = loc.getBlockY();
        int baseZ = loc.getBlockZ();

        int solid = 0;
        int total = 0;

        for (int dx = -radius; dx <= radius; dx++) {
            for (int dz = -radius; dz <= radius; dz++) {
                if (dx == 0 && dz == 0) {
                    continue; // центр не считаем
                }
                total++;

                Block b = world.getBlockAt(baseX + dx, baseY, baseZ + dz);
                if (!b.isPassable()) {
                    solid++;
                }
            }
        }

        if (total == 0) {
            return 0.0;
        }
        return solid / (double) total;
    }

    // Кол-во "стен" сразу вокруг игрока (радиус 1)
    private static int countWallsImmediate(World world, Location loc) {
        int baseX = loc.getBlockX();
        int baseY = loc.getBlockY();
        int baseZ = loc.getBlockZ();

        int solid = 0;

        for (int dx = -1; dx <= 1; dx++) {
            for (int dz = -1; dz <= 1; dz++) {
                if (dx == 0 && dz == 0) {
                    continue;
                }
                Block b = world.getBlockAt(baseX + dx, baseY, baseZ + dz);
                if (!b.isPassable()) {
                    solid++;
                }
            }
        }

        return solid;
    }
}
