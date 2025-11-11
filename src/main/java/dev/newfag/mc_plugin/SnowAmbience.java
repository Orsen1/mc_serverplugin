package dev.newfag.mc_plugin;

import org.bukkit.Bukkit;
import org.bukkit.Sound;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;

import java.util.Random;

public class SnowAmbience {
    private final Plugin plugin;
    private final Random rnd = new Random();
    private int taskId = -1;

    public SnowAmbience(Plugin plugin) { this.plugin = plugin; }

    public void start() {
        if (taskId != -1) return;
        // раз в 3 тика (≈0.15с) проверить игроков; звуки ставим не постоянно, а со случайной задержкой
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {
            for (Player p : Bukkit.getOnlinePlayers()) {
                // если хочешь только “во время шторма”, оставь проверку мира:
                // if (!p.getWorld().hasStorm()) continue;

                if (rnd.nextDouble() < 0.04) { // ~1 раз в ~2с в среднем на игрока
                    var loc = p.getLocation().clone().add(rnd.nextGaussian()*2.5, 1.2 + rnd.nextDouble()*1.5, rnd.nextGaussian()*2.5);
                    // мягкие снежные звуки:
                    p.playSound(loc, Sound.BLOCK_SNOW_HIT, 0.35f, 1.1f + rnd.nextFloat()*0.2f);
                    if (rnd.nextDouble() < 0.3) {
                        p.playSound(loc, Sound.BLOCK_SNOW_PLACE, 0.25f, 1.0f + rnd.nextFloat()*0.2f);
                    }
                }
            }
        }, 1L, 3L);
    }

    public void stop() {
        if (taskId != -1) Bukkit.getScheduler().cancelTask(taskId);
        taskId = -1;
    }
}