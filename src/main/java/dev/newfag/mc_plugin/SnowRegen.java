/*
 * Click nbfs://nbhost/SystemFileSystem/Templates/Licenses/license-default.txt to change this license
 * Click nbfs://nbhost/SystemFileSystem/Templates/Classes/Class.java to edit this template
 */
package dev.newfag.mc_plugin;

import org.bukkit.Material;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.block.BlockBreakEvent;
import org.bukkit.plugin.Plugin;

/**
 *
 * @author 0rsen
 */
public class SnowRegen implements Listener {
    private final SnowRegenQueue queue;

    public SnowRegen(Plugin plugin, SnowRegenQueue queue) {
        this.queue = queue;
    }

    @EventHandler
    public void onBlockBreak(BlockBreakEvent e) {
        var b = e.getBlock();
        var w = b.getWorld();
        // интересуемся только: сломали снег ИЛИ проходимый растительный блок
        boolean snow = b.getType() == Material.SNOW;
        boolean plant = b.isPassable() && b.getType() != Material.AIR;
        if (!(snow || plant)) return;

        queue.enqueue(w, b.getX(), b.getY(), b.getZ());
    }
}
