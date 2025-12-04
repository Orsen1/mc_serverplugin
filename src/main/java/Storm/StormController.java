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
import org.bukkit.entity.Entity;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.plugin.Plugin;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author 0rsen
 */
public class StormController {

    boolean isNight = false;

    private int timeCheckTaskId = -1;
    
    private int fogCheckCounter;

    private final Plugin plugin;
    private final int stormDurationSeconds; // сколько секунд длится буря (можно менять извне)

    private boolean stormActive = false;
    private int tickTaskId = -1;
    private int stopTaskId = -1;

    public StormController(Plugin plugin, int stormDurationSeconds) {
        this.plugin = plugin;
        this.stormDurationSeconds = stormDurationSeconds;
        fogCheckCounter = 0;
    }

    public boolean isStormActive() {
        return stormActive;
    }

    /**
     * Запускаем бурю.
     */
    public void startStorm() {
        if (stormActive) {
            return;
        }
        stormActive = true;

        plugin.getLogger().info("[WinterStorm] ❄ Storm started!");

        // поднимаем лимит накопления снега
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gamerule snowAccumulationHeight 5");

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
        if (!stormActive) {
            return;
        }
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
        Bukkit.dispatchCommand(Bukkit.getConsoleSender(), "gamerule snowAccumulationHeight 2");

        for (Player p : Bukkit.getOnlinePlayers()) {
            removeFog(p);
        }

        // можно убрать туман, но Blindness сам спадёт через 1–2 тика
    }

    /**
     * Один "тик" бури — вызывается раз в ... тиков.
     */
    private void stormTick() {
        if (!stormActive) {
            return;
        }
        
        fogCheckCounter++;
        boolean dofogUpdate = fogCheckCounter >= 15;
        if(dofogUpdate){
            fogCheckCounter = 0;
        }

        for (Player p : Bukkit.getOnlinePlayers()) {
            World w = p.getWorld();
            if (w.getEnvironment() != World.Environment.NORMAL) {
                continue;
            }
            //spawnFog(p, 1000);
            spawnStormSnow(p, 1500);
            switch (PlayerData.getEnvironmentCode(p)) {
                case 1:
                    if (hasFog(p) && dofogUpdate) {
                        removeFog(p);
                    }
                    break;
                case 2:
                    if (hasFog(p) && dofogUpdate) {
                        removeFog(p);
                    }
                    playWindSound(p, 0.75f, 0.25f);
                    break;
                case 3:
                    if (!hasFog(p) && dofogUpdate) {
                        applyFog(p, 3);
                    }
                    playWindSound(p, 1.25f, 0.5f);
                    break;
                case 4:
                    if (!hasFog(p) && dofogUpdate) {
                        applyFog(p, 1);
                    }
                    playWindSound(p, 2f, 0.75f);
                    break;
                case 5:
                    if (!hasFog(p) && dofogUpdate) {
                        applyFog(p, 2);
                    }
                    playWindSound(p, 2.5f, 0.75f);
                    break;
                case 6:
                    if (!hasFog(p) && dofogUpdate) {
                        applyFog(p, 2);
                    }
                    playWindSound(p, 2.5f, 0.75f);
                    break;
                default:
                    if (!hasFog(p) && dofogUpdate) {
                        applyFog(p, 1);
                    }
                    playWindSound(p, 2.5f, 0.75f);
                    break;
            }
        }
    }

    /**
     * Усиленный снег: много частиц, быстрее падают.
     */
    private void spawnStormSnow(Player p, int flakes) {
        World world = p.getWorld();
        Location pl = p.getLocation();

        int radius = 5; // радиус вокруг игрока, где рисуем снег

        int px = pl.getBlockX();
        int py = pl.getBlockY();
        int pz = pl.getBlockZ();
        int maxY = world.getMaxHeight();

        for (int i = 0; i < flakes; i++) {

            // случайная точка по горизонтали вокруг игрока
            int x = px + (int) Math.round((Math.random() * 2 - 1) * radius);
            int z = pz + (int) Math.round((Math.random() * 2 - 1) * radius);

            // 1) ПРОВЕРКА: есть ли над ИГРОКОМ в этой колонке хоть один блок?
            boolean blocked = false;
            for (int y = py+2; y <= maxY; y++) {
                if (!world.getBlockAt(x, y, z).isPassable()) {
                    blocked = true;  // найден блок → колонка под крышей/скалой
                    break;
                }
            }

            // если над этой точкой что-то есть — снег не спавним
            if (blocked) {
                continue;
            }

            // 2) ВЫБИРАЕМ высоту спавна снежинки в свободной колонке
            double minY = py + 2;                         // не прям у головы
            double maxSpawnY = Math.min(py + 10, maxY - 1); // максимум +10 над игроком
            if (minY >= maxSpawnY) {
                continue; // мало места, пропускаем
            }

            double y = minY + Math.random() * (maxSpawnY - minY);

            Location loc = new Location(world, x + 0.5, y, z + 0.5);

            world.spawnParticle(
                    Particle.SNOWFLAKE,
                    loc,
                    1, // одна снежинка
                    0, 0, 0,
                    0.6
            );
        }
    }

    /**
     * Плотный туман: Blindness 1-го уровня, обновляем каждые 10 тиков.
     */
    private void applyFog(Player p, int fogLevel) {
        p.addPotionEffect(new PotionEffect(
                PotionEffectType.BLINDNESS,
                99999999,
                fogLevel, // уровень 1 → очень плотный туман
                false, // ambient
                false, // particles
                false // icon
        ));
    }

    public void removeFog(Player p) {
        p.removePotionEffect(PotionEffectType.BLINDNESS);
    }

    private boolean hasFog(Player p) {
        return p.hasPotionEffect(PotionEffectType.BLINDNESS);
    }

    private void spawnFog(Player p, int strength) {
        World world = p.getWorld();
        Location pl = p.getLocation();

        // strength = сколько частиц за тик
        world.spawnParticle(
                Particle.WHITE_ASH, // или WHITE_ASH, или SPORE_BLOSSOM_AIR
                strength, // сколько частиц
                5, 1.5, 5 // offset по X/Y/Z (радиус тумана)
        );
    }

    /**
     * Свистящий звук метели.
     */
    private void playWindSound(Player p, float loudLevel, float ton) {
        p.playSound(
                p.getLocation(),
                Sound.AMBIENT_SOUL_SAND_VALLEY_ADDITIONS,
                loudLevel, // громкость
                ton // тон
        );
    }

    private void buffMobsDuringStorm() {
        for (World w : Bukkit.getWorlds()) {
            if (w.getEnvironment() != World.Environment.NORMAL) {
                continue;
            }

            for (Entity entity : w.getLivingEntities()) {

                if (!(entity instanceof LivingEntity)) {
                    continue;
                }
                LivingEntity le = (LivingEntity) entity;
                if (le instanceof Player || !(le instanceof Monster)) {
                    return;
                }

                le.addPotionEffect(new PotionEffect(
                        PotionEffectType.SPEED,
                        stormDurationSeconds,
                        2, // уровень 3
                        true, // ambient
                        false, // без частиц
                        false // без иконки
                ));

                // Сила (INCREASE_DAMAGE = Strength)
                le.addPotionEffect(new PotionEffect(
                        PotionEffectType.STRENGTH,
                        stormDurationSeconds,
                        2,
                        true,
                        false,
                        false
                ));
            }
        }
    }

    // -------------------------
    //  НОЧНОЙ АВТО-ЗАПУСК БУРИ
    // -------------------------
    public void startTimeWatcher() {

        timeCheckTaskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, () -> {

            World world = Bukkit.getWorlds().get(0);
            long time = world.getTime();  // 0..24000

            // ночь 13000+
            if (time >= 12000 && !stormActive && !isNight) {

                // шанс 30%
                double i = Math.random();
                System.out.println(i);
                if (i < 0.30) {
                    startStorm();
                }
                isNight = true;
            }

            if (time < 12000 && isNight) {
                isNight = false;
            }
            if (stormActive) {

                if (time < 12000) {
                    stopStorm();
                }
            }

        }, 20L, 5L);
    }
}
