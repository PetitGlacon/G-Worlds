package fr.the__glacier.gworlds.listeners;

import fr.the__glacier.gworlds.GWorlds;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.world.WorldLoadEvent;

public class WorldLoadListener implements Listener {
    @EventHandler
    public void onWorldLoad(WorldLoadEvent event){
        GWorlds.getInstance().getLogger().severe(event.getWorld().getName());
    }
}
