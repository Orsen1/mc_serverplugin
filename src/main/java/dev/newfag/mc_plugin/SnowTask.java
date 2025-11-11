package dev.newfag.mc_plugin;

import java.util.Random;
import org.bukkit.*;
import org.bukkit.block.Block;
import org.bukkit.block.data.Levelled;
import org.bukkit.block.data.type.Snow;
import org.bukkit.entity.Player;
import org.bukkit.plugin.java.JavaPlugin;

/**
 *
 * @author 0rsen
 */
public class SnowTask {

    private final JavaPlugin plugin;
    private int taskId = -1;
    private final Random rnd = new Random();

    public SnowTask(JavaPlugin plugin) {
        this.plugin = plugin;
    }

    public void start() {
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (World world : Bukkit.getWorlds()) {
                if (!world.hasStorm()) {
                    continue;
                }
                for (Player p : world.getPlayers()) {
                    // визуальные снежные частицы для ощущения снегопада
                    world.spawnParticle(Particle.SNOWFLAKE, p.getLocation().clone().add(0, 2.0, 0),
                            120, 12, 6, 12, 0.05);
                    // реальные снежные слои и лёд
                    sprinkleSnow(p);
                    //freezeWater(p);
                }
            }
        }, 20L, 20L); // каждую секунду
    }

    public void stop() {
        if (taskId != -1) {
            Bukkit.getScheduler().cancelTask(taskId);
        }
    }

    private void sprinkleSnow(Player p) {
        World w = p.getWorld();
        Location base = p.getLocation();

        for (int i = 0; i < 60; i++) {
            int x = base.getBlockX() + rnd.nextInt(25) - 12;
            int z = base.getBlockZ() + rnd.nextInt(25) - 12;
            int y = w.getHighestBlockYAt(x, z);

            Block top = w.getBlockAt(x, y, z);
            Block below = w.getBlockAt(x, y - 1, z);

            // кладём ТОЛЬКО если сверху чистый воздух и снизу “полная” опора
            if (!top.getType().isAir()) {
                continue;
            }
            if (!UtilsWinter.canSupportSnow(below)) {
                continue;
            }

            top.setType(Material.SNOW, false);
            var snow = (org.bukkit.block.data.type.Snow) top.getBlockData();
            snow.setLayers(1 + rnd.nextInt(3));
            top.setBlockData(snow, false);
        }
    }
    /*
    private void freezeWater(Player p) {
        World w = p.getWorld();
        Location base = p.getLocation();
        for (int i = 0; i < 50; i++) {
            int x = base.getBlockX() + rnd.nextInt(25) - 12;
            int z = base.getBlockZ() + rnd.nextInt(25) - 12;
            int y = w.getHighestBlockYAt(x, z);
            Block b = w.getBlockAt(x, y - 1, z);
            if (b.getType() == Material.WATER) {
                if (b.getBlockData() instanceof Levelled lvl && lvl.getLevel() == 0) {
                    b.setType(Material.ICE, false);
                } else {
                    b.setType(Material.ICE, false);
                }
            }
        }
    }
    */
}
