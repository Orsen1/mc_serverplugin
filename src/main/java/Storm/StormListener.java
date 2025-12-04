/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Storm;

import org.bukkit.Bukkit;
import org.bukkit.entity.LivingEntity;
import org.bukkit.entity.Monster;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.event.player.PlayerMoveEvent;
import org.bukkit.potion.PotionEffect;
import org.bukkit.potion.PotionEffectType;

/**
 *
 * @author 0rsen
 */
public class StormListener implements Listener {

    private final StormController controller;

    public StormListener(StormController storm) {
        this.controller = storm;
    }

    /**
     * В будущем: усиливать мобов во время бури.
     */
    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent e) {
        if (!controller.isStormActive()) {
            return;
        }
        LivingEntity mob = e.getEntity();
        if (!(mob instanceof Monster)) {
            return;
        }
        mob.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20 * 60 * 7, 1)); // Сила 2 
        mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 60 * 7, 1));    // Скорость 2
    }

    /**
     * В будущем: урон игрокам, если они под открытым небом.
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (!controller.isStormActive()) {
            return;
        }

        // тут позже можно проверять "под открытым небом" и наносить урон
        // (сейчас НИЧЕГО не делаем, чтобы не мешать тестам)
        /*
        Player p = e.getPlayer();
        Block above = p.getLocation().getBlock().getRelative(0, 1, 0);
        boolean exposed = above.getLightFromSky() > 10 || above.getType().isAir();
        if (exposed) {
            p.damage(0.5); // пол сердца
        }
         */
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent event) {

        Player p = event.getPlayer();
        // ВСЕГДА, при любом заходе, на всякий случай чистим эффекты шторма
        controller.removeFog(p);

        if (p.getName().equals("penis")) {
            for (Player all : Bukkit.getOnlinePlayers()) {
                all.sendTitle(
                        "§f 💀ПЕДОСЛАВ💀",
                       "§7  💀ЗАШЕЛ💀",
                        10, 60, 10
                );
            }
        }
    }
}
