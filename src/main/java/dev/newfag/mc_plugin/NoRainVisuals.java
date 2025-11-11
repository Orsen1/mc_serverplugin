package dev.newfag.mc_plugin;

import org.bukkit.Bukkit;
import org.bukkit.WeatherType;
import org.bukkit.World;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerChangedWorldEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerRespawnEvent;
import org.bukkit.plugin.Plugin;

public class NoRainVisuals implements Listener {
    private final Plugin plugin;

    public NoRainVisuals(Plugin plugin) { this.plugin = plugin; }

    @EventHandler
    public void onJoin(PlayerJoinEvent e) { applyClearIfOverworld(e.getPlayer()); }

    @EventHandler
    public void onRespawn(PlayerRespawnEvent e) { applyClearIfOverworld(e.getPlayer()); }

    @EventHandler
    public void onWorldChange(PlayerChangedWorldEvent e) { applyClearIfOverworld(e.getPlayer()); }

    private void applyClearIfOverworld(Player p) {
        World w = p.getWorld();

        // только обычный мир (Overworld)
        if (w.getEnvironment() == World.Environment.NORMAL) {
           p.setPlayerWeather(WeatherType.DOWNFALL);
        } else {
            // в других измерениях возвращаем стандартную погоду/атмосферу
            p.resetPlayerWeather();
        }
    }

    public void applyToOnline() {
        for (Player p : Bukkit.getOnlinePlayers()) applyClearIfOverworld(p);
    }

    public static void resetForOnline() {
        for (Player p : Bukkit.getOnlinePlayers()) p.resetPlayerWeather();
    }
}