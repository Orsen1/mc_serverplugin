package dev.newfag.mc_plugin;

import org.bukkit.Bukkit;
import org.bukkit.Material;
import org.bukkit.World;
import org.bukkit.block.Block;
import org.bukkit.block.data.type.Snow;
import org.bukkit.plugin.Plugin;

import java.util.*;

public class SnowRegenQueue {
    record Task(World w, int x, int y, int z, int triesLeft, long nextTick) {}

    private final Plugin plugin;
    private final Deque<Task> queue = new ArrayDeque<>();
    private final Set<String> dedup = new HashSet<>();
    private final Random rnd = new Random();
    private int taskId = -1;

    // свой счётчик тиков вместо Bukkit.getCurrentTick() (которого нет в Spigot)
    private long tickCounter = 0;

    private static final int PER_TICK = 40;     // лимит операций за тик
    private static final int MAX_QUEUE = 1000;  // ограничение очереди
    private static final int DELAY_MIN = 100;   // ~5 сек
    private static final int DELAY_MAX = 400;   // ~20 сек

    public SnowRegenQueue(Plugin plugin) { this.plugin = plugin; }

    public void start() {
        if (taskId != -1) return;
        taskId = Bukkit.getScheduler().scheduleSyncRepeatingTask(plugin, this::tick, 1L, 1L);
    }

    public void stop() {
        if (taskId != -1) Bukkit.getScheduler().cancelTask(taskId);
        taskId = -1;
        queue.clear();
        dedup.clear();
        tickCounter = 0;
    }

    public void enqueue(World w, int x, int y, int z) {
        if (w == null) return;
        if (queue.size() >= MAX_QUEUE) return;

        String key = w.getName() + ":" + x + ":" + y + ":" + z;
        if (!dedup.add(key)) return; // уже есть задача на эту точку

        long when = tickCounter + DELAY_MIN + rnd.nextInt(Math.max(1, (DELAY_MAX - DELAY_MIN)));
        queue.addLast(new Task(w, x, y, z, 5, when));
    }

    private void tick() {
        tickCounter++; // свой "now"

        long now = tickCounter;
        int processed = 0;
        int size = queue.size();

        for (int i = 0; i < size && processed < PER_TICK; i++) {
            Task t = queue.pollFirst();
            if (t == null) break;

            // ещё рано — вернуть в очередь в конец
            if (t.nextTick > now) {
                queue.addLast(t);
                continue;
            }

            // мир мог выгрузиться или быть null (на всякий)
            if (t.w == null) continue;

            Block top = t.w.getBlockAt(t.x, t.y, t.z);
            Block below = t.w.getBlockAt(t.x, t.y - 1, t.z);

            boolean ok = t.w.hasStorm()
                    && top.getType() == Material.AIR
                    && UtilsWinter.isExposedOrUnderLeaves(top, below);

            if (ok && rnd.nextDouble() < 0.75) {
                top.setType(Material.SNOW, false);
                Snow s = (Snow) top.getBlockData();
                s.setLayers(1);
                top.setBlockData(s, false);

                // успех → забыть ключ
                dedup.remove(t.w.getName() + ":" + t.x + ":" + t.y + ":" + t.z);
            } else if (t.triesLeft > 1) {
                long next = now + DELAY_MIN + rnd.nextInt(Math.max(1, (DELAY_MAX - DELAY_MIN)));
                queue.addLast(new Task(t.w, t.x, t.y, t.z, t.triesLeft - 1, next));
            } else {
                dedup.remove(t.w.getName() + ":" + t.x + ":" + t.y + ":" + t.z);
            }
            processed++;
        }
    }
}