/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package Storm;

import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.entity.CreatureSpawnEvent;
import org.bukkit.event.player.PlayerMoveEvent;

/**
 *
 * @author 0rsen
 */
public class StormListener implements Listener {

    private final StormController storm;

    public StormListener(StormController storm) {
        this.storm = storm;
    }

    /**
     * В будущем: усиливать мобов во время бури.
     */
    @EventHandler
    public void onMobSpawn(CreatureSpawnEvent e) {
        if (!storm.isStormActive()) return;

        // пример поведения НА БУДУЩЕЕ: пока закомментировано
        /*
        LivingEntity mob = e.getEntity();
        mob.addPotionEffect(new PotionEffect(PotionEffectType.STRENGTH, 20 * 60, 0)); // Сила I на минуту
        mob.addPotionEffect(new PotionEffect(PotionEffectType.SPEED, 20 * 60, 0));    // Скорость I на минуту
         */
    }

    /**
     * В будущем: урон игрокам, если они под открытым небом.
     */
    @EventHandler
    public void onPlayerMove(PlayerMoveEvent e) {
        if (!storm.isStormActive()) return;

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
}