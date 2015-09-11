package de.lenic.redsync.listeners;

import de.lenic.redsync.RedSync;
import org.bukkit.entity.Player;
import org.bukkit.event.EventHandler;
import org.bukkit.event.Listener;
import org.bukkit.event.player.PlayerQuitEvent;

public class QuitListener implements Listener {

    // Plugin Instance
    private RedSync plugin;
    public QuitListener(RedSync plugin){
        this.plugin = plugin;
    }

    @EventHandler
    public void onQuit(PlayerQuitEvent e){
        final Player p = e.getPlayer();

        // Bugfix: Clear inventory if dead
        if(p.isDead())
            p.getInventory().clear();

        // Save player data
        this.plugin.getExecutor().execute(new Runnable() {
            @Override
            public void run() {
                plugin.getRedis().savePlayer(p);
                plugin.getMessagingManager().sendSavingCompleted(p);
            }
        });
    }

}
