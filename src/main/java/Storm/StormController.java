/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Storm;

import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.Particle;
import org.bukkit.Sound;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author 0rsen
 */
public class StormController  {

    private final Plugin plugin;
    private final int stormDurationSeconds; // сколько секунд длится буря (можно менять извне)

    private boolean stormActive = false;
    private int tickTaskId = -1;
    private int stopTaskId = -1;

    public StormController(Plugin plugin, int stormDurationSeconds) {
        this.plugin = plugin;
        this.stormDurationSeconds = stormDurationSeconds;
    }

    public boolean isStormActive() {
        return stormActive;
    }

    /**
     * Запускаем бурю.
     */
    public void startStorm() {
        if (stormActive) return;
        stormActive = true;

        plugin.getLogger().info("[WinterStorm] ❄ Storm started!");

        // поднимаем лимит накопления снега
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/gamerule snowAccumulationHeight 20");

        // визуал + звук каждые 10 тиков (0.5 секунды)
        tickTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::stormTick, 1L, 5L);

        // если длительность > 0 — планируем автоматическое окончание
        if (stormDurationSeconds > 0) {
            stopTaskId = Bukkit.getScheduler().scheduleSyncDelayedTask(
                    plugin,
                    this::stopStorm,
                    stormDurationSeconds * 20L
            );
        }
    }

    /**
     * Останавливаем бурю.
     */
    public void stopStorm() {
        if (!stormActive) return;
        stormActive = false;
        plugin.getLogger().info("[WinterStorm] 🌤 Storm ended!");

        if (tickTaskId != -1) {
            Bukkit.getScheduler().cancelTask(tickTaskId);
            tickTaskId = -1;
        }
        if (stopTaskId != -1) {
            Bukkit.getScheduler().cancelTask(stopTaskId);
            stopTaskId = -1;
        }

        // возвращаем стандартный лимит (можешь поменять значение на своё)
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "/gamerule snowAccumulationHeight 3");

        // можно убрать туман, но Blindness сам спадёт через 1–2 тика
    }

    /**
     * Один "тик" бури — вызывается раз в ... тиков.
     */
    private void stormTick() {
        if (!stormActive) return;
        for (Player p: Bukkit.getOnlinePlayers()) {
            World w = p.getWorld();
            if (w.getEnvironment() != World.Environment.NORMAL) continue;
            
            switch (PlayerData.getEnvironmentCode(p)) {
            //nothing
                case 1:
                    System.out.println("1");
                    break;
                case 2:
                    System.out.println("2");
                    playWindSound(p, 0.75f, 0.25f);
                    break;
                case 3:
                    System.out.println("3");
                    spawnStormSnow(p, 500);
                    applyFog(p, 3);
                    playWindSound(p, 1.25f, 0.5f);
                    break;
                case 4:
                    System.out.println("4");
                    spawnStormSnow(p, 2000);
                    applyFog(p, 1);
                    playWindSound(p, 2f, 0.75f);
                    break;
                case 5:
                    System.out.println("5");
                    spawnStormSnow(p, 2000);
                    applyFog(p, 2);
                    playWindSound(p, 2.5f, 0.75f);
                    break;
                case 6:
                    System.out.println("6");
                    spawnStormSnow(p, 4000);
                    applyFog(p, 2);
                    playWindSound(p, 2.5f, 0.75f);
                    break;
                default:
                    System.out.println("7");
                    spawnStormSnow(p, 4000);
                    applyFog(p, 1);
                    playWindSound(p, 2.5f, 0.75f);
                    break;
            }
        }
    }

    /**
     * Усиленный снег: много частиц, быстрее падают.
     */
    private void spawnStormSnow(Player p, int i) {
        // точка чуть над игроком
        var loc = p.getLocation().clone().add(0, 2.5, 0);

        p.spawnParticle(
                Particle.SNOWFLAKE,
                loc,
                5000,      // количество частиц
                5, 5, 5, // разброс по XYZ
                0.8      // "скорость" / сила движения
        );
    }

    /**
     * Плотный туман: Blindness 1-го уровня, обновляем каждые 10 тиков.
     */
    private void applyFog(Player p, int fogLevel) {
        p.addPotionEffect(new PotionEffect(
                PotionEffectType.BLINDNESS,
                stormDurationSeconds,
                fogLevel,    // уровень 1 → очень плотный туман
                false, // ambient
                false, // particles
                false  // icon
        ));
    }

    /**
     * Свистящий звук метели.
     */
    private void playWindSound(Player p, float loudLevel, float ton) {
        p.playSound(
                p.getLocation(),
                Sound.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS,
                loudLevel,  // громкость
                ton  // тон
        );
    }
}