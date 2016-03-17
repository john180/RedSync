package de.lenic.redsync.listeners;

import de.lenic.redsync.RedSyncBungee;
import net.md_5.bungee.api.event.LoginEvent;
import net.md_5.bungee.api.plugin.Listener;
import net.md_5.bungee.event.EventHandler;
import net.md_5.bungee.event.EventPriority;

import java.util.Optional;

public class ConnectListener implements Listener {

    private RedSyncBungee plugin;

    // Constructor
    public ConnectListener(RedSyncBungee plugin) {
        this.plugin = plugin;
    }


    @EventHandler(priority = EventPriority.HIGHEST)
    public void onConnect(LoginEvent e) {
        if(e.isCancelled())
            return;

        plugin.getProxy().getScheduler().runAsync(plugin, () -> {
            final Optional<String> data = plugin.getRedis().getData(e.getConnection().getUniqueId().toString());
            String message;

            // No saved data present
            if(data.isPresent())
                message = data.get();
            else
                message = "{\"owner\":\"" + e.getConnection().getUniqueId().toString() + "\",\"isEmpty\":true}";

            // Publish data
            plugin.getRedis().publish(message);
        });
    }

}
