package dev.newfag.mc_plugin;

import org.bukkit.*;
import org.bukkit.plugin.java.JavaPlugin;

public class Application extends JavaPlugin {

    private SnowTask snowTask;

    @Override
    public void onEnable() {
        // включаем шторм (чтобы сразу шёл снег в холодных биомах)
        for (World w : Bukkit.getWorlds()) {
            w.setStorm(true);
            w.setThundering(false);
            w.setWeatherDuration(20 * 60 * 20); // на 20 минут, сервер сам продлит
        }
        Bukkit.getPluginManager().registerEvents(new WeatherLock(), this);

        Bukkit.getPluginManager().registerEvents(new GlobalWinter(), this);

        // 2) Снежный тикер (частицы + реальные снежные слои/лёд)
        snowTask = new SnowTask(this);
        snowTask.start();

        SnowRegenQueue regenQueue = new SnowRegenQueue(this);
        regenQueue.start();
        Bukkit.getPluginManager().registerEvents(new SnowRegen(this, regenQueue), this);

        NoRainVisuals noRain = new NoRainVisuals(this);
        Bukkit.getPluginManager().registerEvents(noRain, this);
        noRain.applyToOnline();

        SnowAmbience ambience = new SnowAmbience(this);
        ambience.start();
        
        Bukkit.getPluginManager().registerEvents(new VegetationToSnow(), this);
        /*
        // 3) Если ProtocolLib установлен — включаем “подмену биомов для клиента”
        if (Bukkit.getPluginManager().isPluginEnabled("ProtocolLib")) {
            ProtocolLibrary.getProtocolManager().addPacketListener(new SnowyBiomeAdapter(this));
            getLogger().info("ProtocolLib найден — включена подмена биомов для клиента.");
        } else {
            getLogger().warning("ProtocolLib не найден — работаем без спуфинга биома (только частицы + реальные слои).");
        }*/ getLogger
        ().info("WinterVisual (no ProtocolLib) enabled.");
    }

    @Override
    public void onDisable() {
        if (snowTask != null) {
            snowTask.stop();
        }
    }
}
