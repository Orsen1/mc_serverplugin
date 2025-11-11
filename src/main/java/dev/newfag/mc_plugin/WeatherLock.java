package dev.newfag.mc_plugin;

import org.bukkit.World;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.weather.ThunderChangeEvent;
import org.bukkit.event.weather.WeatherChangeEvent;

/**
 *
 * @author 0rsen
 */
public class WeatherLock implements Listener{
    @EventHandler public void onWeather(WeatherChangeEvent e) {
        if (!e.toWeatherState()) {
            e.setCancelled(true);
            World w = e.getWorld();
            w.setStorm(true);
            w.setThundering(false);
            w.setWeatherDuration(20 * 60 * 20);
        }
    }
    @EventHandler public void onThunder(ThunderChangeEvent e) {
        if (e.toThunderState()) e.setCancelled(true);
    }
}
