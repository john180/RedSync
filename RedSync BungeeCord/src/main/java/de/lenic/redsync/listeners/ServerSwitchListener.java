package de.lenic.redsync.listeners;

import de.lenic.redsync.RedSyncBungee;
import net.md_5.bungee.api.event.ServerConnectEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.Optional;

public class ServerSwitchListener implements Listener {

    private RedSyncBungee plugin;

    // Constructor
    public ServerSwitchListener(RedSyncBungee plugin) {
        this.plugin = plugin;
    }

    @EventHandler(priority = EventPriority.HIGHEST)
    public void onServerSwitch(ServerConnectEvent e) {
        if(e.isCancelled())
            return;

        if(e.getPlayer().getServer() == null)
            return;

        if(!plugin.getConfig().getStringList("Excluded-Servers").contains(e.getPlayer().getServer().getInfo().getName()))
            return;

        final Optional<String> data = plugin.getRedis().getData(e.getPlayer().getUniqueId().toString());
        String message;

        // No saved data present
        if(data.isPresent())
            message = data.get();
        else
            message = "{\"owner\":\"" + e.getPlayer().getUniqueId().toString() + "\",\"isEmpty\":true}";

        // Publish data
        plugin.getRedis().publish(message);
    }

}
