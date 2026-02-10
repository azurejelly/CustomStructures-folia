package com.ryandw11.structure.threading;

import com.ryandw11.structure.CustomStructures;
import com.ryandw11.structure.structure.Structure;
import com.ryandw11.structure.structure.StructureHandler;
import com.ryandw11.structure.utils.Pair;
import io.papermc.paper.threadedregions.scheduler.ScheduledTask;
import org.bukkit.Bukkit;
import org.bukkit.Location;
import org.bukkit.scheduler.BukkitRunnable;

import java.util.HashSet;
import java.util.Map;
import java.util.Set;
import java.util.concurrent.TimeUnit;

/**
 * This ensures that there is not a memory leak with the structure list.
 */
public class CheckStructureList {

    public static final int MAX_STORED_STRUCTURES = 300;

    private final StructureHandler handler;
    private ScheduledTask task;

    public CheckStructureList(StructureHandler handler) {
        this.handler = handler;
    }

    public void init() {
        task = Bukkit.getAsyncScheduler().runAtFixedRate(CustomStructures.getInstance(), (t) -> {
            synchronized (handler.getSpawnedStructures()) {
                Set<Pair<Location, Long>> locationsToRemove = new HashSet<>();
                for (Map.Entry<Pair<Location, Long>, Structure> entry : handler.getSpawnedStructures().entrySet()) {
                    if (System.currentTimeMillis() - entry.getKey().getRight() > 2.592e+8) {
                        locationsToRemove.add(entry.getKey());
                    } else if (handler.getSpawnedStructures().size() - locationsToRemove.size() > MAX_STORED_STRUCTURES)
                        locationsToRemove.add(entry.getKey());
                }
                handler.getSpawnedStructures().keySet().removeAll(locationsToRemove);
            }
        }, 1, 300, TimeUnit.SECONDS);
    }

    public void cancel() {
        if (task != null) {
            task.cancel();
            task = null;
        }
    }
}
