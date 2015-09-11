package de.lenic.redsync.listeners;

import de.lenic.redsync.RedSync;
import de.lenic.redsync.RedSyncConfig;
import org.bukkit.Bukkit;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerJoinEvent;
import org.bukkit.metadata.FixedMetadataValue;

public class JoinListener implements Listener {

    // Plugin Instance
    private RedSync plugin;
    public JoinListener(RedSync plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onJoin(PlayerJoinEvent e){
        final Player p = e.getPlayer();

        // Lock player
        p.setMetadata(LockListener.lockedMeta, new FixedMetadataValue(this.plugin, true));

        // Unlock player after load delay
        Bukkit.getScheduler().runTaskLaterAsynchronously(this.plugin, new Runnable() {
            @Override
            public void run() {
                plugin.getRedis().loadPlayer(p);

                // Unlock player
                synchronized (p){
                    p.removeMetadata(LockListener.lockedMeta, plugin);
                }
            }
        }, RedSyncConfig.getLoadDelay());
    }

}
